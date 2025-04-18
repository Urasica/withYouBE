package com.capstone.withyou.service;

import com.capstone.withyou.dto.StockCurPriceDTO;
import com.capstone.withyou.dto.StockPriceDTO;
import com.capstone.withyou.dto.StockPriceDayDTO;
import com.capstone.withyou.dto.WatchListStockPriceDTO;
import com.capstone.withyou.utils.StockApiClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockPriceService {
    private static final String STOCK_CACHE_PREFIX = "stock:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final StockApiClient stockApiClient;

    private final int timeOut = 2;

    /**
     * 국내 주식 기간별 시세 조회
     */
    public List<StockPriceDTO> getDomesticStockPricesByDay(String stockCode, String period) {
        String cacheKey = STOCK_CACHE_PREFIX + stockCode + ":" + period;
        try {
            String cachedData = redisTemplate.opsForValue().get(cacheKey);

            if (cachedData != null) {
                return objectMapper.readValue(cachedData, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.error("Redis 연결 오류: {}", e.getMessage(), e);
        }

        String url = "/uapi/domestic-stock/v1/quotations/inquire-daily-price"
                + "?FID_COND_MRKT_DIV_CODE=J"
                + "&FID_INPUT_ISCD=" + stockCode
                + "&FID_PERIOD_DIV_CODE=" + period
                + "&FID_ORG_ADJ_PRC=0";

        List<StockPriceDTO> stockPrices = new ArrayList<>();
        String response = stockApiClient.getData(url,"FHKST01010400" );

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode outputNode = rootNode.path("output");

            if (!outputNode.isArray()) {
                log.error("국내 주식 데이터 형식 오류: {}", rootNode.toPrettyString());
                return stockPrices;
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
            log.error("JSON 파싱 오류: {}", e.getMessage(), e);
            return stockPrices;
        }

        try {
            if (!stockPrices.isEmpty())
                redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(stockPrices), timeOut, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("Redis 캐싱 실패 ({}): {}", stockCode, e.getMessage(), e);
        }

        return stockPrices;
    }

    /**
     * 해외 주식 기간별 시세 조회
     */
    public List<StockPriceDTO> getOverseasStockPriceByDay(String stockCode, String period) {
        String cacheKey = STOCK_CACHE_PREFIX + stockCode + ":" + period;
        try {
            String cachedData = redisTemplate.opsForValue().get(cacheKey);

            if (cachedData != null) {
                return objectMapper.readValue(cachedData, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.error("Redis 연결 오류: {}", e.getMessage(), e);
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

        List<StockPriceDTO> stockPrices = new ArrayList<>();
        String response = stockApiClient.getData(url,"HHDFS76240000");

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode outputNode = rootNode.path("output2");

            if (!outputNode.isArray()) {
                log.error("해외 주식 데이터 형식 오류: {}", rootNode.toPrettyString());
                return stockPrices;
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
            log.error("JSON 파싱 오류: {}", e.getMessage(), e);
            return stockPrices;
        }

        try {
            if (!stockPrices.isEmpty())
                redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(stockPrices), timeOut, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("Redis 캐싱 실패 ({}): {}", stockCode, e.getMessage(), e);
        }

        return stockPrices;
    }

    /**
     * 국내 주식 분봉 조회
     */
    public List<StockPriceDayDTO> getDomesticStockPricesDistribution(String stockCode, String time) {
        String cacheKey = STOCK_CACHE_PREFIX + stockCode + ":Day:" + time;

        try {
            String cachedData = redisTemplate.opsForValue().get(cacheKey);

            if (cachedData != null) {
                return objectMapper.readValue(cachedData, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.error("Redis 연결 오류: {}", e.getMessage(), e);
        }

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = today.format(formatter);

        String url = "/uapi/domestic-stock/v1/quotations/inquire-time-dailychartprice"
                + "?FID_COND_MRKT_DIV_CODE=J"
                + "&FID_INPUT_ISCD=" + stockCode
                + "&FID_INPUT_DATE_1=" + formattedDate
                + "&FID_INPUT_HOUR_1="
                + "&FID_PW_DATA_INCU_YN=Y"
                + "&FID_FAKE_TICK_INCU_YN=N";

        List<StockPriceDayDTO> stockPrices = new ArrayList<>();
        String response = stockApiClient.getData(url,"FHKST03010230" );

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            if (!rootNode.has("output2") || !rootNode.get("output2").isArray()) {
                log.error("주식 데이터 형식 오류: {}", rootNode.toPrettyString());
                return stockPrices;
            }
            JsonNode outputNode = rootNode.get("output2");

            for (JsonNode node : outputNode) {
                stockPrices.add(new StockPriceDayDTO(
                        node.get("stck_bsop_date").asText(),
                        node.get("stck_cntg_hour").asText(),
                        node.get("stck_oprc").asDouble(),
                        node.get("stck_hgpr").asDouble(),
                        node.get("stck_lwpr").asDouble(),
                        node.get("stck_prpr").asDouble(),
                        node.get("cntg_vol").asLong(),
                        node.get("acml_tr_pbmn").asLong()
                ));
            }
        } catch (Exception e) {
            log.error("JSON 파싱 오류: {}", e.getMessage(), e);
            return stockPrices;
        }

        try {
            if (!stockPrices.isEmpty())
                redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(stockPrices), timeOut, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("Redis 캐싱 실패 ({}): {}", stockCode, e.getMessage(), e);
        }

        return stockPrices;
    }

    /**
     * 해외 주식 분봉 조회
     */
    public List<StockPriceDayDTO> getOverseasStockPricesDistribution(String stockCode, String time) {
        String cacheKey = STOCK_CACHE_PREFIX + stockCode + ":Day:" + time;

        try {
            String cachedData = redisTemplate.opsForValue().get(cacheKey);

            if (cachedData != null) {
                return objectMapper.readValue(cachedData, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.error("Redis 연결 오류: {}", e.getMessage(), e);
        }

        String url = "/uapi/overseas-price/v1/quotations/inquire-time-itemchartprice"
                + "?AUTH="
                + "&EXCD=NAS"
                + "&SYMB=" + stockCode
                + "&NMIN=" + time
                + "&NREC=120"
                + "&PINC=0"
                + "&NEXT="
                + "&FILL="
                + "&KEYB=";

        List<StockPriceDayDTO> stockPrices = new ArrayList<>();
        String response = stockApiClient.getData(url,"HHDFS76950200");

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            if (!rootNode.has("output2") || !rootNode.get("output2").isArray()) {
                log.error("해외 주식 데이터 형식 오류: {}", rootNode.toPrettyString());
                return stockPrices;
            }
            JsonNode outputNode = rootNode.get("output2");

            for (JsonNode node : outputNode) {
                stockPrices.add(new StockPriceDayDTO(
                        node.get("kymd").asText(),
                        node.get("khms").asText(),
                        node.get("open").asDouble(),
                        node.get("high").asDouble(),
                        node.get("low").asDouble(),
                        node.get("last").asDouble(),
                        node.get("evol").asLong(),
                        node.get("eamt").asLong()
                ));
            }
        } catch (Exception e) {
            log.error("JSON 파싱 오류: {}", e.getMessage(), e);
            return stockPrices;
        }

        try {
            if (!stockPrices.isEmpty())
                redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(stockPrices), timeOut, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("Redis 캐싱 실패 ({}): {}", stockCode, e.getMessage(), e);
        }

        return stockPrices;
    }

    /**
     * 국내 주식 현재가 조회
     */
    public StockCurPriceDTO getDomesticStockCurPrice(String stockCode) {
        String cacheKey = STOCK_CACHE_PREFIX + stockCode + ":" + "NOW";
        try {
            String cachedData = redisTemplate.opsForValue().get(cacheKey);

            if (cachedData != null) {
                return objectMapper.readValue(cachedData, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.error("Redis 연결 오류: {}", e.getMessage(), e);
        }

        String url = "/uapi/domestic-stock/v1/quotations/inquire-ccnl"
                + "?FID_COND_MRKT_DIV_CODE=J"
                + "&FID_INPUT_ISCD=" + stockCode;

        StockCurPriceDTO stockPrice;
        String response = stockApiClient.getData(url,"FHKST01010300");

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode outputNode = rootNode.path("output");

            if (!outputNode.isArray() || outputNode.isEmpty()) {
                log.error("국내 주식 데이터 형식 오류: {}", rootNode.toPrettyString());
                return null;
            }

            JsonNode latestNode = outputNode.get(0);  // 첫 번째 데이터

            if (latestNode == null || latestNode.isNull()) {
                log.error("국내 주식 데이터가 비어 있음: {}", rootNode.toPrettyString());
                throw new RuntimeException("JSON 파싱 오류: latestNode가 null입니다");
            }

            stockPrice = new StockCurPriceDTO(
                    latestNode.get("stck_prpr").asDouble(), // 주식 현재가
                    latestNode.get("prdy_vrss").asDouble(), // 전일 대비 가격
                    latestNode.get("prdy_ctrt").asDouble()  // 전일 대비율
            );

        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 오류: " + e.getMessage(), e);
        }

        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(stockPrice), timeOut, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("Redis 캐싱 실패 ({}): {}", stockCode, e.getMessage(), e);
        }

        return stockPrice;
    }

    /**
     * 해외 주식 현재가 조회
     */
    public StockCurPriceDTO getOverseasStockCurPrice(String stockCode) {
        String cacheKey = STOCK_CACHE_PREFIX + stockCode + ":" + "NOW";
        try {
            String cachedData = redisTemplate.opsForValue().get(cacheKey);

            if (cachedData != null) {
                return objectMapper.readValue(cachedData, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.error("Redis 연결 오류: {}", e.getMessage(), e);
        }

        String url = "/uapi/overseas-price/v1/quotations/price"
                + "?AUTH="
                + "&EXCD=NAS"
                + "&SYMB=" + stockCode;

        StockCurPriceDTO stockPrice;
        String response = stockApiClient.getData(url,"HHDFS00000300");

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode outputNode = rootNode.path("output");

            if (outputNode.isObject()) {
                stockPrice = new StockCurPriceDTO(
                        outputNode.get("last").asDouble(),  // 주식 현재가
                        outputNode.get("diff").asDouble(),  // 전일 대비 가격
                        outputNode.get("rate").asDouble()   // 전일 대비율
                );
            } else {
                log.error("해외 주식 데이터 형식 오류: {}", rootNode.toPrettyString());
                return null;
            }

        } catch (Exception e) {
            log.error("JSON 파싱 오류: {}", e.getMessage(), e);
            return null;
        }

        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(stockPrice), timeOut, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("Redis 캐싱 실패 ({}): {}", stockCode, e.getMessage(), e);
        }

        return stockPrice;
    }

    /**
     * 관심 종목 국내 주식 현재가 조회
     */
    public WatchListStockPriceDTO getDomesticWatchListStockCurPrice(String stockCode) {
        String cacheKey = STOCK_CACHE_PREFIX + stockCode + ":" + "WatchList";
        try {
            String cachedData = redisTemplate.opsForValue().get(cacheKey);

            if (cachedData != null) {
                return objectMapper.readValue(cachedData, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.error("Redis 연결 오류: {}", e.getMessage(), e);
        }

        String url = "/uapi/domestic-stock/v1/quotations/inquire-price"
                + "?FID_COND_MRKT_DIV_CODE=J"
                + "&FID_INPUT_ISCD=" + stockCode;

        WatchListStockPriceDTO stockPrice;
        String response = stockApiClient.getData(url,"FHKST01010100");

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode outputNode = rootNode.path("output");

            stockPrice = new WatchListStockPriceDTO(
                    stockCode,
                    "",
                    outputNode.get("stck_prpr").asText(), // 주식 현재가
                    outputNode.get("prdy_vrss").asText(), // 전일 대비 가격
                    outputNode.get("prdy_ctrt").asText(),  // 전일 대비율
                    outputNode.get("acml_vol").asText(),
                    outputNode.get("prdy_vrss_vol_rate").asText()
            );
        } catch (Exception e) {
            log.error("JSON 파싱 오류: {}", e.getMessage(), e);
            stockPrice = new WatchListStockPriceDTO();
            stockPrice.setStockCode(stockCode);
            stockPrice.setStockName("주식 코드 잘못됨");
            return stockPrice;
        }

        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(stockPrice), timeOut, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("Redis 캐싱 실패 ({}): {}", stockCode, e.getMessage(), e);
            stockPrice = new WatchListStockPriceDTO();
            return stockPrice;
        }

        return stockPrice;
    }

    /**
     * 관심 종목 해외 주식 현재가 조회
     */
    public WatchListStockPriceDTO getOverseasWatchListStockCurPrice(String stockCode) {
        String cacheKey = STOCK_CACHE_PREFIX + stockCode + ":" + "WatchList";
        try {
            String cachedData = redisTemplate.opsForValue().get(cacheKey);

            if (cachedData != null) {
                return objectMapper.readValue(cachedData, new TypeReference<>() {});
            }
        } catch (Exception e) {
            // Redis 연결 오류 로그
            System.err.println("Redis 연결 오류: " + e.getMessage());
        }

        String url = "/uapi/overseas-price/v1/quotations/price"
                + "?AUTH="
                + "&EXCD=NAS"
                + "&SYMB=" + stockCode;

        String response = stockApiClient.getData(url,"HHDFS00000300");

        WatchListStockPriceDTO stockPrice;
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode outputNode = rootNode.path("output");

            if (outputNode.isObject()) {
                stockPrice = new WatchListStockPriceDTO(
                        stockCode,
                        "",
                        outputNode.get("last").asText(),  // 주식 현재가
                        outputNode.get("diff").asText(),  // 전일 대비 가격
                        outputNode.get("rate").asText(),  // 전일 대비율
                        outputNode.get("tvol").asText(),
                        outputNode.get("tamt").asText()
                );
            } else {
                return new WatchListStockPriceDTO();
            }

        } catch (Exception e) {
            stockPrice = new WatchListStockPriceDTO();
            return stockPrice;
        }

        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(stockPrice), timeOut, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("Redis 캐싱 실패 ({}): {}", stockCode, e.getMessage(), e);
            stockPrice = new WatchListStockPriceDTO();
            return stockPrice;
        }

        return stockPrice;
    }
}