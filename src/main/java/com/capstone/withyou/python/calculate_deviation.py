import FinanceDataReader as fdr
from concurrent.futures import ThreadPoolExecutor
import numpy as np
import math

# 나스닥 전체 주식 목록 가져오기
df_nasdaq = fdr.StockListing("NASDAQ")
symbols = df_nasdaq['Symbol'].tolist()

# 수치 계산 함수
def get_stock_score(symbol):
    try:
        df = fdr.DataReader(symbol)
        if len(df) < 2:
            return symbol, None

        latest = df.iloc[-1]
        prev = df.iloc[-2]

        open_price = latest['Open']
        close_price = latest['Close']
        high = latest['High']
        low = latest['Low']
        volume = latest['Volume']
        adj_close = latest['Adj Close']

        # 등락률
        change = (close_price - open_price) / open_price

        # 강한 마감 비율
        if high != low:
            close_strength = (close_price - low) / (high - low)
        else:
            close_strength = 0.5  # 변동 없는 날

        # 변동성
        volatility = (high - low) / open_price

        # 로그 거래량 (0 방지)
        log_volume = math.log(volume + 1)

        # 수익률 보정
        if close_price != 0:
            adj_ratio = adj_close / close_price
        else:
            adj_ratio = 1

        # 가중치 조합
        score = (
            0.3 * change +
            0.2 * close_strength +
            0.2 * volatility +
            0.2 * log_volume / 15 +
            0.1 * adj_ratio
        )

        return symbol, round(score, 4)

    except Exception:
        return symbol, None

results = {}
with ThreadPoolExecutor(max_workers=10) as executor:
    for symbol, score in executor.map(get_stock_score, symbols):
        if score is not None:
            results[symbol] = score

# 결과 출력
for symbol, score in list(results.items())[:]:
    print(symbol, score)