package com.capstone.withyou.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class AppConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder, RateLimiter rateLimiter) {
        return builder.baseUrl("https://openapi.koreainvestment.com:9443")
                .filter((request, next) ->
                        next.exchange(request)
                                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                )
                .build();
    }

    @Bean
    public RateLimiter rateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1)) // 1초마다 리미트 리셋
                .limitForPeriod(10) // 초당 최대 10회 요청 가능
                .timeoutDuration(Duration.ofSeconds(1))
                .build();

        return RateLimiter.of("stockApiRateLimiter", config);
    }
}
