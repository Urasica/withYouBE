import pandas as pd
import FinanceDataReader as fdr
import sys
sys.stdout.reconfigure(encoding='utf-8')

def update_stock_data():
    try:
        # NASDAQ 데이터 가져오기
        df_nasdaq = fdr.StockListing("NASDAQ")[["Symbol", "Name", "Industry"]]

        # KRX 데이터 가져오기
        df_krx = fdr.StockListing("KRX")[["Code", "Name"]].rename(columns={"Code": "Symbol"})
        df_krx["Industry"] = None

        # 두 데이터 합치기
        df_stock = pd.concat([df_nasdaq, df_krx], ignore_index=True)

        # 종목코드와 종목명 출력
        for index, row in df_stock.iterrows():
            industry = row['Industry'] if row['Industry'] else "N/A"
            print(f"{row['Symbol']}|{row['Name']}|{industry}")

    except Exception as e:
        print(f"\n에러 발생: {str(e)}")

if __name__ == "__main__":
    update_stock_data()
