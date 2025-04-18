import FinanceDataReader as fdr
import math
import time
import random
import logging
import signal
import sys
import multiprocessing

# 시그널 핸들링 (Docker 종료 시 등)
def graceful_exit(signum, frame):
    logger.warning(f"프로세스 종료 시그널 감지 (signal {signum}). 종료 중...")
    sys.exit(0)

signal.signal(signal.SIGINT, graceful_exit)
signal.signal(signal.SIGTERM, graceful_exit)

# 로깅 설정
logging.basicConfig(
    filename='deviation.log',
    filemode='w',
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(message)s'
)
logger = logging.getLogger()

# 점수 계산 함수 (서브프로세스에서 실행)
def worker(symbol, return_dict):
    try:
        df = fdr.DataReader(symbol, '2025-04-01')
        if len(df) < 2:
            return_dict['status'] = 'warning'
            return_dict['message'] = "데이터 부족"
            return

        latest = df.iloc[-1]
        open_price = latest['Open']
        close_price = latest['Close']
        high = latest['High']
        low = latest['Low']
        volume = latest['Volume']
        adj_close = latest.get('Adj Close', close_price)

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
        return_dict['status'] = 'success'
        return_dict['score'] = round(score, 4)

    except Exception as e:
        return_dict['status'] = 'error'
        return_dict['message'] = str(e)

# 점수 요청 처리
def get_score_with_timeout(symbol, index):
    logger.info(f"[{index}] {symbol}: 요청 시작")
    time.sleep(random.uniform(0.2, 0.3))

    manager = multiprocessing.Manager()
    return_dict = manager.dict()

    p = multiprocessing.Process(target=worker, args=(symbol, return_dict))
    p.start()
    p.join(timeout=3)

    if p.is_alive():
        p.terminate()
        p.join()
        logger.warning(f"[{index}] {symbol}: 타임아웃 발생 - 프로세스 강제 종료")
        return None

    status = return_dict.get("status")
    if status == 'success':
        score = return_dict.get("score")
        logger.info(f"[{index}] {symbol}: 점수 계산 완료 = {score}")
        return score
    elif status == 'warning':
        logger.warning(f"[{index}] {symbol}: {return_dict.get('message')}")
    elif status == 'error':
        logger.error(f"[{index}] {symbol}: 예외 발생 - {return_dict.get('message')}")
    return None

# 실행 시작
if __name__ == '__main__':
    df_nasdaq = fdr.StockListing("NASDAQ")
    symbols = df_nasdaq['Symbol'].tolist()

    results = {}

    for idx, symbol in enumerate(symbols):
        # 100건마다 잠시 대기
        if idx > 0 and idx % 100 == 0:
            logger.info(f"\n=== 요청 {idx}건 완료. 5초 대기 중... ===\n")
            time.sleep(5)

        score = get_score_with_timeout(symbol, idx + 1)
        if score is not None:
            results[symbol] = score

    # 결과 출력
    for symbol, score in sorted(results.items(), key=lambda x: x[1], reverse=True):
        print(f"{symbol} {score:.4f}")