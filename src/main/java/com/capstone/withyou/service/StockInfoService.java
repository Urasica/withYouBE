package com.capstone.withyou.service;

import com.capstone.withyou.dao.StockInfo;
import com.capstone.withyou.dto.StockInfoDTO;
import com.capstone.withyou.repository.StockInfoRepository;
import com.capstone.withyou.utils.StockApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class StockInfoService {
    private final StockInfoRepository stockInfoRepository;
    private final ObjectMapper objectMapper;
    private final StockApiClient stockApiClient;

    public StockInfoService(StockInfoRepository stockInfoRepository,
                            StockApiClient stockApiClient,
                            ObjectMapper objectMapper) {
        this.stockInfoRepository = stockInfoRepository;
        this.objectMapper = objectMapper;
        this.stockApiClient = stockApiClient;
    }

    public StockInfoDTO getDomesticStockInfo(String stockCode) {
        StockInfo stock = stockInfoRepository.findByStockCode(stockCode).orElse(null);
        LocalDateTime oneHourAgo = LocalDateTime.now().minusMinutes(1);

        if(stock != null) {
            if (!stock.getLastUpdated().isBefore(oneHourAgo)) {
                return convertToDto(stock);
            }
        }

        String url = "/uapi/domestic-stock/v1/quotations/inquire-price"
                + "?FID_INPUT_ISCD=" + stockCode
                + "&FID_COND_MRKT_DIV_CODE=J";


        String response = stockApiClient.getData(url, "FHKST01010100");

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

                stockInfoRepository.save(stock);
                return convertToDto(stock);
            } else {
                log.error("데이터 형식 오류: {}", rootNode.toPrettyString());
            }

        } catch (Exception e) {
            log.error("JSON 파싱 오류: {}", e.getMessage(), e);
        }

        StockInfoDTO defaultDto = new StockInfoDTO();
        defaultDto.setStockCode(stockCode);
        return defaultDto;
    }

    public StockInfoDTO getOverseasStockInfo(String stockCode) {
        StockInfo stock = stockInfoRepository.findByStockCode(stockCode).orElse(null);
        LocalDateTime oneHourAgo = LocalDateTime.now().minusMinutes(1);
        if(stock != null) {
            if (!stock.getLastUpdated().isBefore(oneHourAgo)) {
                return convertToDto(stock);
            }
        }

        String url = "/uapi/overseas-price/v1/quotations/price-detail"
                + "?AUTH="
                + "&EXCD=NAS"
                + "&SYMB=" + stockCode;

        String response = stockApiClient.getData(url, "HHDFS76200200");

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

                stockInfoRepository.save(stock);
                return convertToDto(stock);
            } else {
                log.error("데이터 형식 오류: {}", rootNode.toPrettyString());
            }

        } catch (Exception e) {
            log.error("JSON 파싱 오류: {}", e.getMessage(), e);
        }

        StockInfoDTO defaultDto = new StockInfoDTO();
        defaultDto.setStockCode(stockCode);
        return defaultDto;
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
