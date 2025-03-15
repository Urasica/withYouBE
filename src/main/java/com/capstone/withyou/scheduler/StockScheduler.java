package com.capstone.withyou.scheduler;

import com.capstone.withyou.service.StockService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StockScheduler {

    private final StockService stockService;

    public StockScheduler(StockService stockService) {
        this.stockService = stockService;
    }

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    public void scheduledUpdateStockData() {
        try {
            stockService.updateStockData();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
