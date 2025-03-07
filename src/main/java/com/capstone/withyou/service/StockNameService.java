package com.capstone.withyou.service;

import com.capstone.withyou.dao.StockInfo;
import com.capstone.withyou.repository.StockNameRepository;
import org.springframework.stereotype.Service;

@Service
public class StockNameService {

    private final StockNameRepository stockNameRepository;
    private final StockNameCrawler stockNameCrawler;

    public StockNameService(StockNameRepository stockNameRepository, StockNameCrawler stockNameCrawler) {
        this.stockNameRepository = stockNameRepository;
        this.stockNameCrawler = stockNameCrawler;
    }

    public String getStockName(String stockCode) {
        StockInfo stockInfo = stockNameRepository.findByStockCode(stockCode);

        if(stockInfo != null && stockInfo.getStockName()!=null) {
            return stockInfo.getStockName();
        }

        // 최초 조회 시
        String stockName = stockNameCrawler.crawlStockName(stockCode);

        StockInfo newStockInfo = new StockInfo();
        newStockInfo.setStockCode(stockCode);
        newStockInfo.setStockName(stockName);

        stockNameRepository.save(newStockInfo);

        return stockName;
    }
}
