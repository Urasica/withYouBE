import FinanceDataReader as fdr
from concurrent.futures import ThreadPoolExecutor, TimeoutError
import math
from tqdm import tqdm

# 나스닥 종목 리스트 가져오기
df_nasdaq = fdr.StockListing("NASDAQ")
symbols = df_nasdaq['Symbol'].tolist()

# 종목 점수 계산 함수
def get_stock_score(symbol):
    try:
        df = fdr.DataReader(symbol, '2025-04-01')
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

    except:
        return None

# 배치 단위 지정
BATCH_SIZE = 300
results = {}

# 전체 배치 반복
for i in range(0, len(symbols), BATCH_SIZE):
    batch = symbols[i:i+BATCH_SIZE]
    with tqdm(total=len(batch), desc=f"배치 처리 중 ({i//BATCH_SIZE+1})") as pbar:
        with ThreadPoolExecutor(max_workers=15) as executor:
            future_to_symbol = {executor.submit(get_stock_score, symbol): symbol for symbol in batch}

            for future in future_to_symbol:
                symbol = future_to_symbol[future]
                try:
                    score = future.result(timeout=5)
                    if score is not None:
                        results[symbol] = score
                except TimeoutError:
                    pass
                except Exception:
                    pass
                finally:
                    pbar.update(1)

# 결과 출력
for symbol, score in sorted(results.items(), key=lambda x: x[1], reverse=True):
    print(f"{symbol} {score:.4f}")
