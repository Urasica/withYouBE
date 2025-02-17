package com.capstone.withyou.service;

import com.capstone.withyou.Manager.AccessTokenManager;
import com.capstone.withyou.dto.StockPriceDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class StockPriceService {
    private static final String STOCK_CACHE_PREFIX = "stock:";

    @Value("${api.kis.appkey}")
    private String appKey;

    @Value("${api.kis.appsecret}")
    private String appSecret;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AccessTokenManager tokenManager;
    private final WebClient webClient;

    public StockPriceService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper, AccessTokenManager tokenManager, WebClient webClient) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.tokenManager = tokenManager;
        this.webClient = webClient;
    }

    /**
     * 국내 주식 시세 조회
     */
    public List<StockPriceDTO> getDomesticStockPricesByDay(String stockCode, String period) {
        String cacheKey = STOCK_CACHE_PREFIX + stockCode + ":" + period;
        try {
            String cachedData = redisTemplate.opsForValue().get(cacheKey);

            if (cachedData != null) {
                return objectMapper.readValue(cachedData, new TypeReference<>() {});
            }
        } catch (Exception e) {
            // Redis 연결 오류 로그
            System.err.println("Redis 연결 오류: " + e.getMessage());
        }

        String url = "/uapi/domestic-stock/v1/quotations/inquire-daily-price"
                + "?FID_COND_MRKT_DIV_CODE=J"
                + "&FID_INPUT_ISCD=" + stockCode
                + "&FID_PERIOD_DIV_CODE=" + period
                + "&FID_ORG_ADJ_PRC=0";

        String response = webClient.get()
                .uri(url)
                .headers(headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.set("Authorization", "Bearer " + tokenManager.getAccessToken());
                    headers.set("appkey", appKey);
                    headers.set("appsecret", appSecret);
                    headers.set("tr_id", "FHKST01010400");
                    headers.set("tr_cont", "");
                    headers.set("custtype", "P");
                })
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<StockPriceDTO> stockPrices = new ArrayList<>();
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode outputNode = rootNode.path("output");

            if (!outputNode.isArray()) {
                throw new RuntimeException("국내 주식 데이터 형식 오류: " + rootNode.toPrettyString());
            }

            for (JsonNode node : outputNode) {
                stockPrices.add(new StockPriceDTO(
                        node.get("stck_bsop_date").asText(),
                        node.get("stck_oprc").asInt(),
                        node.get("stck_hgpr").asInt(),
                        node.get("stck_lwpr").asInt(),
                        node.get("stck_clpr").asInt(),
                        node.get("acml_vol").asLong()
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 오류: " + e.getMessage(), e);
        }

        try {
            if (!stockPrices.isEmpty())
                redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(stockPrices), 3, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return stockPrices;
    }

    /**
     * 해외 주식 시세 조회
     */
    public List<StockPriceDTO> getOverseasPriceByDay(String stockCode, String period) {
        System.out.println("getOverseasPriceByDay");

        String cacheKey = STOCK_CACHE_PREFIX + stockCode + ":" + period;
        try {
            String cachedData = redisTemplate.opsForValue().get(cacheKey);

            if (cachedData != null) {
                return objectMapper.readValue(cachedData, new TypeReference<>() {});
            }
        } catch (Exception e) {
            // Redis 연결 오류 로그
            System.err.println("Redis 연결 오류: " + e.getMessage());
        }

        int day = switch (period) {
            case "D" -> 0;
            case "W" -> 1;
            case "M" -> 2;
            default -> throw new IllegalStateException("Unexpected value: " + period);
        };

        String url = "/uapi/overseas-price/v1/quotations/dailyprice"
                + "?AUTH="
                + "&EXCD=NAS"
                + "&SYMB=" + stockCode
                + "&GUBN=" + day
                + "&BYMD="
                + "&MODP=1";

        String response = webClient.get()
                .uri(url)
                .headers(headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.set("Authorization", "Bearer " + tokenManager.getAccessToken());
                    headers.set("appkey", appKey);
                    headers.set("appsecret", appSecret);
                    headers.set("tr_id", "HHDFS76240000");
                    headers.set("tr_cont", "");
                    headers.set("custtype", "P");
                })
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<StockPriceDTO> stockPrices = new ArrayList<>();
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode outputNode = rootNode.path("output2");

            if (!outputNode.isArray()) {
                throw new RuntimeException("해외 주식 데이터 형식 오류: " + rootNode.toPrettyString());
            }

            for (JsonNode node : outputNode) {
                stockPrices.add(new StockPriceDTO(
                        node.get("xymd").asText(),
                        node.get("open").asDouble(),
                        node.get("high").asDouble(),
                        node.get("low").asDouble(),
                        node.get("clos").asDouble(),
                        node.get("tvol").asLong()
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 오류: " + e.getMessage(), e);
        }

        try {
            if (!stockPrices.isEmpty())
                redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(stockPrices), 3, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return stockPrices;
    }
}
