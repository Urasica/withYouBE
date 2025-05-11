import FinanceDataReader as fdr
from concurrent.futures import ThreadPoolExecutor, as_completed
import math
from tqdm import tqdm
import logging
import time
import random

# 로깅 설정
logging.basicConfig(
    filename='stock_score.log',
    filemode='w',  # 매 실행마다 로그 덮어쓰기
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(message)s'
)
logger = logging.getLogger(__name__)

# 나스닥 종목 리스트 가져오기
df_nasdaq = fdr.StockListing("NASDAQ")
symbols = df_nasdaq['Symbol'].tolist()

# 종목 점수 계산 함수
def get_stock_score(symbol):
    try:
        time.sleep(random.uniform(0.1, 0.2))
        df = fdr.DataReader(symbol, '2025-05-01')
        if len(df) < 2:
            return None

        latest = df.iloc[-1]
        open_price = latest['Open']
        close_price = latest['Close']
        high = latest['High']
        low = latest['Low']
        volume = latest['Volume']
        adj_close = latest['Adj Close']

        change = (close_price - open_price) / open_price
        close_strength = (close_price - low) / (high - low) if high != low else 0.5
        volatility = (high - low) / open_price
        log_volume = math.log(volume + 1)
        adj_ratio = adj_close / close_price if close_price != 0 else 1

        score = (
            0.3 * change +
            0.2 * close_strength +
            0.2 * volatility +
            0.2 * log_volume / 15 +
            0.1 * adj_ratio
        )
        return round(score, 4)

    except Exception as e:
        logger.error(f"{symbol}: 예외 발생 - {e}")
        return None

# 배치 단위 설정
BATCH_SIZE = 100
results = {}

for i in range(0, len(symbols), BATCH_SIZE):
    batch = symbols[i:i + BATCH_SIZE]
    logger.info(f"\n=== 배치 {i // BATCH_SIZE + 1} 시작: 총 {len(batch)}개 ===")

    with tqdm(total=len(batch), desc=f"배치 처리 중 ({i // BATCH_SIZE + 1})") as pbar:
        with ThreadPoolExecutor(max_workers=3) as executor:
            futures = {executor.submit(get_stock_score, symbol): symbol for symbol in batch}
            completed_count = 0

            try:
                for future in as_completed(futures, timeout=90):  # 배치당 최대 대기 시간
                    symbol = futures[future]
                    try:
                        score = future.result(timeout=5)
                        if score is not None:
                            results[symbol] = score
                            logger.info(f"{symbol}: 점수 계산 완료 = {score}")
                        else:
                            logger.warning(f"{symbol}: 점수 없음")
                    except Exception as e:
                        logger.warning(f"{symbol}: 작업 실패 - {e}")
                    finally:
                        completed_count += 1
                        pbar.update(1)

            except Exception as e:
                logger.error(f"배치 {i // BATCH_SIZE + 1}: 일부 작업 타임아웃 또는 실패 - {e}")
                # 남은 작업 수만큼 강제로 bar 업데이트
                pbar.update(len(batch) - completed_count)

    logger.info(f"배치 {i // BATCH_SIZE + 1} 완료. 10초 대기 후 다음 배치로 이동합니다.")
    time.sleep(10)

# 최종 결과 출력
for symbol, score in sorted(results.items(), key=lambda x: x[1], reverse=True):
    print(f"{symbol} {score:.4f}")