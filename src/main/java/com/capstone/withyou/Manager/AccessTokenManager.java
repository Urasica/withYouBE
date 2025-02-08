package com.capstone.withyou.Manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class AccessTokenManager {

    @Value("${api.kis.appkey}")
    private String appKey;

    @Value("${api.kis.appsecret}")
    private String appSecret;

    @Value("${api.kis.token-url}")
    private String tokenUrl;

    private static final String TOKEN_FILE = "access_token.json";
    private String accessToken;
    private LocalDateTime expiresAt;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 토큰을 가져오거나, 만료되었으면 새로 발급
     */
    public synchronized String getAccessToken() {
        if (accessToken == null || isTokenExpired()) {
            loadTokenFromFile();
            if (accessToken == null || isTokenExpired()) {
                requestNewToken();
                saveTokenToFile();
            }
        }
        return accessToken;
    }

    /**
     * 토큰이 만료되었는지 확인
     */
    private boolean isTokenExpired() {
        return expiresAt == null || LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 파일에서 토큰을 불러오기
     */
    private void loadTokenFromFile() {
        try {
            File file = new File(TOKEN_FILE);
            if (file.exists()) {
                Map<String, String> tokenData = objectMapper.readValue(file, Map.class);
                this.accessToken = tokenData.get("access_token");
                this.expiresAt = LocalDateTime.parse(tokenData.get("expires_at"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        } catch (IOException e) {
            System.err.println("토큰 파일을 불러오는 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 토큰을 파일에 저장
     */
    private void saveTokenToFile() {
        try {
            Map<String, String> tokenData = new HashMap<>();
            tokenData.put("access_token", accessToken);
            tokenData.put("expires_at", expiresAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            objectMapper.writeValue(new File(TOKEN_FILE), tokenData);
        } catch (IOException e) {
            System.err.println("토큰 저장 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 한국투자증권 API에 접근하여 새로운 토큰 요청
     */
    private void requestNewToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("appkey", appKey);
        body.put("appsecret", appSecret);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                tokenUrl, HttpMethod.POST, request, Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
            this.accessToken = (String) responseBody.get("access_token");

            // "YYYY-MM-DD HH:mm:ss" 형식의 만료 시간 파싱
            String expiresAtStr = (String) responseBody.get("access_token_token_expired");
            this.expiresAt = LocalDateTime.parse(expiresAtStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            saveTokenToFile();
            System.out.println("새로운 토큰 발급: " + accessToken);
        } else {
            throw new RuntimeException("토큰 발급 실패: " + response.getStatusCode());
        }
    }
}
