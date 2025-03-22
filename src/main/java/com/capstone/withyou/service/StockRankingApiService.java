package com.capstone.withyou.service;

import com.capstone.withyou.dao.*;
import com.capstone.withyou.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class StockRankingApiService {
    private final StockRankDomesticRiseRepository stockRankDomesticRiseRepository;
    private final StockRankDomesticFallRepository stockRankDomesticFallRepository;
    private final StockRankDomesticTradeRepository stockRankDomesticTradeRepository;
    private final StockRankOverseasRiseRepository stockRankOverseasRiseRepository;
    private final StockRankOverseasFallRepository stockRankOverseasFallRepository;
    private final StockRankOverseasTradeRepository stockRankOverseasTradeRepository;

    public String generateResponseChangeRate(String scope, int rank, String metric) {
        Object stock = null;
        if (scope.equals("국내")) {
            if (metric.equals("상승률"))
                stock = stockRankDomesticRiseRepository.findByRank(rank);
            else if (metric.equals("하락률"))
                stock = stockRankDomesticFallRepository.findByRank(rank);
            else if (metric.equals("거래량"))
                stock = stockRankDomesticTradeRepository.findByRank(rank);
        } else {
            if (metric.equals("상승률"))
                stock = stockRankOverseasRiseRepository.findByRank(rank);
            else if (metric.equals("하락률"))
                stock = stockRankOverseasFallRepository.findByRank(rank);
            else if (metric.equals("거래량"))
                stock = stockRankOverseasTradeRepository.findByRank(rank);
        }

        Map<String, Object> stockData = extractStockData(stock);

        if (stock == null) {
            return "주식 데이터를 찾을 수 없습니다.";
        }

        String scopeText = "국내".equals(scope) ? "국내 주식" : "해외 주식";
        String stockName = stockData.get("stockName").toString();
        String stockCode = stockData.get("stockCode").toString();
        String currentPrice = stockData.get("currentPrice").toString();
        String changeRate = stockData.get("changeRate").toString();
        String tradeVolume = stockData.get("tradeVolume").toString();

        String metricText = switch (metric) {
            case "상승률" -> String.format("등락률이 %s%%", changeRate);
            case "하락률" -> String.format("하락률이 %s%%", changeRate);
            case "거래량" -> String.format("거래량이 %s", tradeVolume);
            default -> "해당 지표를 제공할 수 없습니다.";
        };

        return String.format(
                "현재 %s에서 %s이 %d위인 종목은 %s(%s)입니다. 현재가는 %s원이며, %s입니다.",
                scopeText, metric, rank, stockName, stockCode, currentPrice, metricText
        );
    }

    private Map<String, Object> extractStockData(Object stock) {
        Map<String, Object> data = new HashMap<>();
        if (stock instanceof StockRankDomesticRise rise) {
            data.put("stockName", rise.getStockName());
            data.put("stockCode", rise.getStockCode());
            data.put("currentPrice", rise.getCurrentPrice());
            data.put("changeRate", rise.getChangeRate());
            data.put("tradeVolume", rise.getTradeVolume());
        } else if (stock instanceof StockRankDomesticFall fall) {
            data.put("stockName", fall.getStockName());
            data.put("stockCode", fall.getStockCode());
            data.put("currentPrice", fall.getCurrentPrice());
            data.put("changeRate", fall.getChangeRate());
            data.put("tradeVolume", fall.getTradeVolume());
        } else if (stock instanceof StockRankDomesticTrade trade) {
            data.put("stockName", trade.getStockName());
            data.put("stockCode", trade.getStockCode());
            data.put("currentPrice", trade.getCurrentPrice());
            data.put("changeRate", trade.getChangeRate());
            data.put("tradeVolume", trade.getTradeVolume());
        } else if (stock instanceof StockRankOverseasRise rise) {
            data.put("stockName", rise.getStockName());
            data.put("stockCode", rise.getStockCode());
            data.put("currentPrice", rise.getCurrentPrice());
            data.put("changeRate", rise.getChangeRate());
            data.put("tradeVolume", rise.getTradeVolume());
        } else if (stock instanceof StockRankOverseasFall fall) {
            data.put("stockName", fall.getStockName());
            data.put("stockCode", fall.getStockCode());
            data.put("currentPrice", fall.getCurrentPrice());
            data.put("changeRate", fall.getChangeRate());
            data.put("tradeVolume", fall.getTradeVolume());
        } else if (stock instanceof StockRankOverseasTrade trade) {
            data.put("stockName", trade.getStockName());
            data.put("stockCode", trade.getStockCode());
            data.put("currentPrice", trade.getCurrentPrice());
            data.put("changeRate", trade.getChangeRate());
            data.put("tradeVolume", trade.getTradeVolume());
        }
        return data;
    }
}
