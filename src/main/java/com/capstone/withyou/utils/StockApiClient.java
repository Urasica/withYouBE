package com.capstone.withyou.utils;

import com.capstone.withyou.Manager.AccessTokenManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class StockApiClient {
    private final RestTemplate restTemplate;
    private final RateLimiter rateLimiter;
    private final AccessTokenManager tokenManager;
    private final String appKey;
    private final String appSecret;

    private static String baseUrl = "https://openapi.koreainvestment.com:9443";
    private static long lastRequestTime = 0;
    private static final Object lock = new Object();

    public String getData(String url, String tradeCode) {
        synchronized (lock) {
            long now = System.currentTimeMillis();
            long delay = Math.max(0, lastRequestTime + 400 - now);
            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            lastRequestTime = System.currentTimeMillis();
        }

        Supplier<String> decorated = RateLimiter.decorateSupplier(rateLimiter, () -> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + tokenManager.getAccessToken());
                headers.set("appkey", appKey);
                headers.set("appsecret", appSecret);
                headers.set("tr_id", tradeCode);
                headers.set("custtype", "P");

                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(
                        baseUrl+url,
                        HttpMethod.GET,
                        entity,
                        String.class
                );

                String body = response.getBody();
                logMsg1(body);
                return body;

            } catch (Exception e) {
                System.err.println("Request failed: " + e.getMessage());
                return "{\"rt_cd\":\"E\",\"msg_cd\":\"FATAL\",\"msg1\":\"" + e.getMessage() + "\"}";
            }
        });

        return decorated.get();
    }

    private void logMsg1(String body) {
        try {
            JsonNode node = new ObjectMapper().readTree(body);
            String rtCd = node.path("rt_cd").asText("");
            String msg1 = node.path("msg1").asText("상세 메시지 없음");

            if (!rtCd.equalsIgnoreCase("0")) { // 실패한 경우만 로그 출력
                System.out.println("Failure message: " + msg1);
            } else {
                System.out.println("Success message: " + msg1); // 성공한 경우도 필요하면 출력
            }
        } catch (Exception e) {
            System.err.println("Failed to parse response for msg1: " + e.getMessage());
        }
    }
}
