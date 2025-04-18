package com.capstone.withyou.config;

import com.capstone.withyou.Manager.AccessTokenManager;
import com.capstone.withyou.utils.StockApiClient;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class ApiClientFactory {

    @Value("${api.kis.appkey}")
    private String appKey;

    @Value("${api.kis.appsecret}")
    private String appSecret;

    @Bean
    public RateLimiter stockApiRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(10)  // 실제 안정선 고려
                .timeoutDuration(Duration.ofSeconds(1))
                .build();

        return RateLimiter.of("stockApi", config);
    }

    @Bean
    public StockApiClient stockApiClient(
            RestTemplate restTemplate,
            RateLimiter stockApiRateLimiter,
            AccessTokenManager accessTokenManager) {

        return new StockApiClient(restTemplate, stockApiRateLimiter, accessTokenManager, appKey, appSecret);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
