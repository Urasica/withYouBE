import FinanceDataReader as fdr

def get_exchange_rate():
    try:
        df = fdr.DataReader('USD/KRW').iloc[-1][0]
        print(df)
    except Exception as e:
        print(f"Error: {str(e)}")

if __name__ == "__main__":
    get_exchange_rate()
