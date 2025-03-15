package com.capstone.withyou.service;

import com.capstone.withyou.dto.ChatgptRequestDTO;
import com.capstone.withyou.dto.ChatgptResponseDTO;
import com.capstone.withyou.dto.NewsDTO;
import com.capstone.withyou.dto.StockInfoDTO;
import com.capstone.withyou.utils.StockNameCorrector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Service
public class ApiService {
    private final NewsService newsService;
    private final StockInfoService stockInfoService;
    private final StockPriceService stockPriceService;
    private final StockRankingService stockRankingService;
    private final StockNameCorrector stockNameCorrector;

    @Value("${api.open-ai}")
    private String apiKey;

    public ApiService(NewsService newsService,
                      StockInfoService stockInfoService,
                      StockPriceService stockPriceService,
                      StockRankingService stockRankingService,
                      StockNameCorrector stockNameCorrector) {
        this.newsService = newsService;
        this.stockInfoService = stockInfoService;
        this.stockPriceService = stockPriceService;
        this.stockRankingService = stockRankingService;
        this.stockNameCorrector = stockNameCorrector;
    }

    public String getStockCurPrice(String stockName) {
        String stockCode = stockNameCorrector.correctStockName(stockName);
        if (stockCode == null) {
            return "조회에 실패했습니다. 종목이름: " + stockName;
        }

        if (stockCode.chars().allMatch(Character::isDigit)) { // 숫자로만 구성된 경우 국내 주식으로 처리
            return stockName + "의 현재 가격은 " + (int)stockPriceService.getDomesticStockCurPrice(stockCode).getStockPrice() + "원 입니다.";
        } else if (stockCode.chars().allMatch(Character::isLetterOrDigit)) { // 숫자 또는 알파벳으로 구성된 경우 해외 주식으로 처리
            return stockName + "의 현재 가격은 " + stockPriceService.getOverseasStockCurPrice(stockCode).getStockPrice() + "달러 입니다.";
        } else {
            return "조회에 실패했습니다. 종목코드: " + stockCode;
        }
    }

    public String getRisingStocks(String scope, String rank) {
        return "";
    }

    public String getFallingStocks(String scope, String rank) {
        return "";
    }

    public String getTradeRankStocks(String scope, String rank) {
        return "";
    }

    public String getNews(String stockName) {
        List<NewsDTO> news = newsService.getNews(stockName,"주식 주가", 1);
        String answer = "가장 최신 뉴스들로 " + news.get(0).getTitle() + " " + news.get(1).getTitle()
                + " " + news.get(2).getTitle() + "등이 있습니다. 자세한 뉴스는 모의투자 탭에서 해당 주식을 검색해보세요.";
        return answer;
    }

    public String getStockInfo(String stockName) {
        String stockCode = stockNameCorrector.correctStockName(stockName);
        if (stockCode == null) {
            return "조회에 실패했습니다. 종목이름: " + stockName;
        }

        StockInfoDTO stockInfo;
        if (stockCode.chars().allMatch(Character::isDigit)) { // 숫자로만 구성된 경우 국내 주식으로 처리
            stockInfo = stockInfoService.getDomesticStockInfo(stockCode);
        } else if (stockCode.chars().allMatch(Character::isLetterOrDigit)) { // 숫자 또는 알파벳으로 구성된 경우 해외 주식으로 처리
            stockInfo = stockInfoService.getOverseasStockInfo(stockCode);
        } else {
            return "조회에 실패했습니다. 종목코드: " + stockCode;
        }
        return stockName + "의 상세 정보입니다."
                + "\n- 주식 코드: " + stockCode
                + "\n- 전일가격: " + stockInfo.getPdpr()
                + "\n- 누적 거래량: " + stockInfo.getTvol()
                + "\n- 누적 거래대금: " + stockInfo.getTamt()
                + "\n- 52주 최고/최저: " + stockInfo.getH52p() + "/" + stockInfo.getL52p()
                + "\n- PER: " + stockInfo.getPer() + ", PBR: " + stockInfo.getPbr() + ", EPS: " + stockInfo.getEps() + ", BPS: " + stockInfo.getBps();
    }
}
