package com.capstone.withyou.service;

import com.capstone.withyou.Manager.AccessTokenManager;
import com.capstone.withyou.dao.StockRankingRise;
import com.capstone.withyou.repository.StockRankingRiseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class StockRankingService {

    @Value("${api.kis.appkey}")
    private String appKey;

    @Value("${api.kis.appsecret}")
    private String appSecret;

    private final StockRankingRiseRepository stockRankingRiseRepository;
    private final AccessTokenManager tokenManager;

    public StockRankingService(StockRankingRiseRepository stockRankingRiseRepository, AccessTokenManager tokenManager) {
        this.stockRankingRiseRepository = stockRankingRiseRepository;
        this.tokenManager = tokenManager;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void fetchAndSaveStockRanking() {
        String url = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/ranking/fluctuation"
                + "?fid_cond_mrkt_div_code=J"
                + "&fid_cond_scr_div_code=20170"
                + "&fid_input_iscd=0000"
                + "&fid_rank_sort_cls_code=2"
                + "&fid_input_cnt_1=0"
                + "&fid_prc_cls_code=0"
                + "&fid_input_price_1="
                + "&fid_input_price_2="
                + "&fid_vol_cnt="
                + "&fid_trgt_cls_code=0"
                + "&fid_trgt_exls_cls_code=0"
                + "&fid_div_cls_code=0"
                + "&fid_rsfl_rate1="
                + "&fid_rsfl_rate2=";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + tokenManager.getAccessToken());
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", "FHPST01700000");
        headers.set("tr_cont", "0");
        headers.set("custtype", "P");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        JsonNode responseBody = response.getBody();
        System.out.println("HTTP Response: " + response.getStatusCode());
        System.out.println("Response Body: " + responseBody);

        if (responseBody != null) {
            JsonNode rankings = responseBody.get("output");

            if (rankings != null) {
                List<StockRankingRise> newStockList = new ArrayList<>();
                for (JsonNode ranking : rankings) {
                    StockRankingRise stockRankingRise = new StockRankingRise();
                    stockRankingRise.setStockCode(ranking.get("stck_shrn_iscd").asText());
                    stockRankingRise.setRank(ranking.get("data_rank").asInt());
                    stockRankingRise.setStockName(ranking.get("hts_kor_isnm").asText());
                    stockRankingRise.setCurrentPrice(ranking.get("stck_prpr").asInt());
                    stockRankingRise.setChangePrice(ranking.get("prdy_vrss").asInt());
                    stockRankingRise.setChangeRate(new BigDecimal(ranking.get("prdy_ctrt").asText()));
                    stockRankingRise.setTradeVolume(ranking.get("acml_vol").asLong());
                    stockRankingRise.setHighestPrice(ranking.get("stck_hgpr").asInt());
                    stockRankingRise.setLowestPrice(ranking.get("stck_lwpr").asInt());

                    newStockList.add(stockRankingRise);
                }

                // prdy_ctrt(changeRate) 기준으로 내림차순 정렬
                newStockList.sort((a, b) -> b.getChangeRate().compareTo(a.getChangeRate()));

                // 정렬 후 순위 재할당
                for (int i = 0; i < newStockList.size(); i++) { newStockList.get(i).setRank(i + 1); }

                stockRankingRiseRepository.deleteAll(); // 기존 데이터 삭제
                stockRankingRiseRepository.saveAll(newStockList); // 한 번에 저장
            }
        }
    }
}
