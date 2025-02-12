package com.capstone.withyou.scheduler;

import com.capstone.withyou.service.StockRankingService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StockRankingInitializer implements ApplicationRunner {

    private final StockRankingService stockRankingService;

    public StockRankingInitializer(StockRankingService stockRankingService) {
        this.stockRankingService = stockRankingService;
    }

    @Override
    public void run(ApplicationArguments args) {
        stockRankingService.fetchAndSaveLongTermStockRankings();
    }
}

