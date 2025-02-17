package com.capstone.withyou.scheduler;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StockRankingInitializer implements ApplicationRunner {

    private final StockRankingScheduler stockRankingScheduler;

    public StockRankingInitializer(StockRankingScheduler stockRankingScheduler) {
        this.stockRankingScheduler = stockRankingScheduler;
    }

    @Override
    public void run(ApplicationArguments args) {
        stockRankingScheduler.fetchAndSaveLongTermStockRankings();
    }
}

