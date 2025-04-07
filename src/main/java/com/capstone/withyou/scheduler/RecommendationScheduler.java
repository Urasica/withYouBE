package com.capstone.withyou.scheduler;

import com.capstone.withyou.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecommendationScheduler {

    private final RecommendationService recommendationService;

    public RecommendationScheduler(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    //매일 새벽 2시에 실행
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateRecommendation() {
        try {
            recommendationService.updateCategoryDeviations();
        } catch (Exception e) {
            log.error("Error during update: {}", e.getMessage(), e);
        }
        log.info("Update completed");
    }
}
