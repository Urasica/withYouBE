package com.capstone.withyou.service;

import com.capstone.withyou.Manager.AccessTokenManager;
import com.capstone.withyou.dao.StockInfo;
import com.capstone.withyou.dto.StockInfoDTO;
import com.capstone.withyou.repository.StockInfoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class StockInfoService {
    private final StockInfoRepository stockInfoRepository;
    private final AccessTokenManager tokenManager;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${api.kis.appkey}")
    private String appKey;

    @Value("${api.kis.appsecret}")
    private String appSecret;

    public StockInfoService(StockInfoRepository stockInfoRepository,
                            AccessTokenManager tokenManager,
                            WebClient webClient,
                            ObjectMapper objectMapper) {
        this.stockInfoRepository = stockInfoRepository;
        this.tokenManager = tokenManager;
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    private Mono<String> fetchStockData(String url, String tradeCode) {
        return webClient.get()
                .uri(url)
                .headers(headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.set("Authorization", "Bearer " + tokenManager.getAccessToken());
                    headers.set("appkey", appKey);
                    headers.set("appsecret", appSecret);
                    headers.set("tr_id", tradeCode);
                    headers.set("custtype", "P");
                })
                .retrieve()
                .bodyToMono(String.class);
    }

    public StockInfoDTO getDomesticStockInfo(String stockCode) {
        StockInfo stock = stockInfoRepository.findByStockCode(stockCode).orElse(null);
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        if(stock != null) {
            if (!stock.getLastUpdated().isBefore(oneHourAgo)) {
                return convertToDto(stock);
            }
        }

        String url = "/uapi/domestic-stock/v1/quotations/inquire-price"
                + "?FID_INPUT_ISCD=" + stockCode
                + "&FID_COND_MRKT_DIV_CODE=J";


        String response = fetchStockData(url, "FHKST01010100").block();

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode outputNode = rootNode.path("output");

            if (outputNode.isObject()) {
                stock = new StockInfo();
                stock.setStockCode(stockCode);
                stock.setPdpr(String.valueOf((outputNode.get("stck_prpr").asInt() + outputNode.get("prdy_vrss").asInt())));
                stock.setOppr(outputNode.get("stck_oprc").asText());
                stock.setHypr(outputNode.get("stck_hgpr").asText());
                stock.setLopr(outputNode.get("stck_lwpr").asText());
                stock.setTvol(outputNode.get("acml_vol").asText());
                stock.setTamt(outputNode.get("acml_tr_pbmn").asText());
                stock.setTomv(outputNode.get("hts_avls").asText());
                stock.setH52p(outputNode.get("w52_hgpr").asText());
                stock.setL52p(outputNode.get("w52_lwpr").asText());
                stock.setPer(outputNode.get("per").asText());
                stock.setPbr(outputNode.get("pbr").asText());
                stock.setEps(outputNode.get("eps").asText());
                stock.setBps(outputNode.get("bps").asText());
                stock.setLastUpdated(LocalDateTime.now());
            } else {
                throw new RuntimeException("데이터 형식 오류: " + rootNode.toPrettyString());
            }

        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 오류: " + e.getMessage(), e);
        }

        stockInfoRepository.save(stock);
        return convertToDto(stock);
    }

    public StockInfoDTO getOverseasStockInfo(String stockCode) {
        StockInfo stock = stockInfoRepository.findByStockCode(stockCode).orElse(null);
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        if(stock != null) {
            if (!stock.getLastUpdated().isBefore(oneHourAgo)) {
                return convertToDto(stock);
            }
        }

        String url = "/uapi/overseas-price/v1/quotations/price-detail"
                + "?AUTH="
                + "&EXCD=NAS"
                + "&SYMB=" + stockCode;

        String response = fetchStockData(url, "HHDFS76200200").block();

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode outputNode = rootNode.path("output");

            if (outputNode.isObject()) {
                stock = new StockInfo();
                stock.setStockCode(stockCode);
                stock.setPdpr(outputNode.get("base").asText());
                stock.setOppr(outputNode.get("open").asText());
                stock.setHypr(outputNode.get("high").asText());
                stock.setLopr(outputNode.get("low").asText());
                stock.setTvol(outputNode.get("tvol").asText());
                stock.setTamt(outputNode.get("tamt").asText());
                stock.setTomv(outputNode.get("tomv").asText());
                stock.setH52p(outputNode.get("h52p").asText());
                stock.setL52p(outputNode.get("l52p").asText());
                stock.setPer(outputNode.get("perx").asText());
                stock.setPbr(outputNode.get("pbrx").asText());
                stock.setEps(outputNode.get("epsx").asText());
                stock.setBps(outputNode.get("bpsx").asText());
                stock.setLastUpdated(LocalDateTime.now());
            } else {
                throw new RuntimeException("데이터 형식 오류: " + rootNode.toPrettyString());
            }

        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 오류: " + e.getMessage(), e);
        }

        stockInfoRepository.save(stock);
        return convertToDto(stock);
    }

    private StockInfoDTO convertToDto(StockInfo stockInfo) {
        StockInfoDTO dto = new StockInfoDTO();
        dto.setStockCode(stockInfo.getStockCode());
        dto.setPdpr(stockInfo.getPdpr());
        dto.setOppr(stockInfo.getOppr());
        dto.setHypr(stockInfo.getHypr());
        dto.setLopr(stockInfo.getLopr());
        dto.setTvol(stockInfo.getTvol());
        dto.setTamt(stockInfo.getTamt());
        dto.setTomv(stockInfo.getTomv());
        dto.setH52p(stockInfo.getH52p());
        dto.setL52p(stockInfo.getL52p());
        dto.setPer(stockInfo.getPer());
        dto.setPbr(stockInfo.getPbr());
        dto.setEps(stockInfo.getEps());
        dto.setBps(stockInfo.getBps());
        return dto;
    }
}
