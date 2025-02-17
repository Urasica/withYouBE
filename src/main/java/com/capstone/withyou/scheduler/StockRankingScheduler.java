package com.capstone.withyou.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import com.capstone.withyou.service.StockRankingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StockRankingScheduler {

    private final StockRankingService stockRankingService;
    private static final Semaphore semaphore = new Semaphore(1);
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public StockRankingScheduler(StockRankingService stockRankingService) {
        this.stockRankingService = stockRankingService;
    }

    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    public void scheduleDailyStockRanking() {
        enqueueTask(stockRankingService::fetchAndSaveDailyStockRankings);
    }

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
    public void scheduleLongTermStockRanking() {
        enqueueTask(stockRankingService::fetchAndSaveLongTermStockRankings);
    }

    public void fetchAndSaveLongTermStockRankings() {
        enqueueTask(stockRankingService::fetchAndSaveLongTermStockRankings);
    }

    private void enqueueTask(Runnable task) {
        executor.submit(() -> {
            try {
                semaphore.acquire();
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        });
    }
}

