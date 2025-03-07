package com.capstone.withyou.service;

import com.capstone.withyou.dao.Stock;
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
        Stock stock = stockNameRepository.findByStockCode(stockCode);

        if(stock != null && stock.getStockName()!=null) {
            return stock.getStockName();
        }

        // 최초 조회 시
        String stockName = stockNameCrawler.crawlStockName(stockCode);

        Stock newStock = new Stock();
        newStock.setStockCode(stockCode);
        newStock.setStockName(stockName);

        stockNameRepository.save(newStock);

        return stockName;
    }
}
