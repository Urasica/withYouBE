package com.capstone.withyou.service;

import com.capstone.withyou.Manager.AccessTokenManager;
import com.capstone.withyou.dao.StockPeriod;
import com.capstone.withyou.dao.StockRank;
import com.capstone.withyou.dao.StockRankDomesticFall;
import com.capstone.withyou.dao.StockRankDomesticRise;
import com.capstone.withyou.repository.StockRankDomesticFallRepository;
import com.capstone.withyou.repository.StockRankDomesticRiseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class StockRankingService {

    @Value("${api.kis.appkey}")
    private String appKey;

    @Value("${api.kis.appsecret}")
    private String appSecret;

    private final StockRankDomesticRiseRepository stockRankDomesticRiseRepository;
    private final StockRankDomesticFallRepository stockRankDomesticFallRepository;
    private final AccessTokenManager tokenManager;
    private static final int API_CALL_INTERVAL = 500;

    public StockRankingService(StockRankDomesticRiseRepository stockRankDomesticRiseRepository,
                               AccessTokenManager tokenManager,
                               StockRankDomesticFallRepository stockRankDomesticFallRepository) {
        this.stockRankDomesticRiseRepository = stockRankDomesticRiseRepository;
        this.tokenManager = tokenManager;
        this.stockRankDomesticFallRepository = stockRankDomesticFallRepository;
    }


    @Scheduled(fixedRate = 60000)
    @Transactional
    public void fetchAndSaveStockRankings() {
//        try {
//            for (StockPeriod period : StockPeriod.values()) {
//                DomesticStockRankRiseFall(0, period);
//                TimeUnit.MILLISECONDS.sleep(API_CALL_INTERVAL); // 1초 대기
//                DomesticStockRankRiseFall(1, period);
//                TimeUnit.MILLISECONDS.sleep(API_CALL_INTERVAL); // 1초 대기
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        DomesticStockRankRiseFall(0, StockPeriod.DAILY);
        DomesticStockRankRiseFall(1, StockPeriod.DAILY);
    }

    /**
     * 국내 주식 등락률 순위 (상승률:0, 하락률:1)
     */
    public void DomesticStockRankRiseFall(int rankSortCode, StockPeriod period) {
        String url = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/ranking/fluctuation"
                + "?fid_cond_mrkt_div_code=J"
                + "&fid_cond_scr_div_code=20170"
                + "&fid_input_iscd=0000"
                + "&fid_rank_sort_cls_code=" + rankSortCode /*순위정렬 구분코드 0:상승율순 1:하락율순*/
                + "&fid_input_cnt_1=" /*+ period.getDays()*/ /*누적일*/
                + "&fid_prc_cls_code=1" /*가격 구분코드 1:종가대비 */
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
        headers.set("tr_cont", "");
        headers.set("custtype", "P");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        JsonNode responseBody = response.getBody();
        System.out.println(responseBody);

        if (responseBody != null) {
            JsonNode rankings = responseBody.get("output");

            if (rankings != null) {
                List<StockRank> stockList = new ArrayList<>();
                for (JsonNode ranking : rankings) {
                    StockRank StockRank = rankSortCode == 0 ? new StockRankDomesticRise() : new StockRankDomesticFall();
                    StockRank.setStockCode(ranking.get("stck_shrn_iscd").asText());
                    StockRank.setRank(ranking.get("data_rank").asInt());
                    StockRank.setStockName(ranking.get("hts_kor_isnm").asText());
                    StockRank.setCurrentPrice(ranking.get("stck_prpr").asInt());
                    StockRank.setChangePrice(ranking.get("prdy_vrss").asInt());
                    StockRank.setChangeRate(new BigDecimal(ranking.get("prdy_ctrt").asText()));
                    StockRank.setTradeVolume(ranking.get("acml_vol").asLong());
                    StockRank.setHighestPrice(ranking.get("stck_hgpr").asInt());
                    StockRank.setLowestPrice(ranking.get("stck_lwpr").asInt());
                    StockRank.setPeriod(period);

                    stockList.add(StockRank);
                }
                System.out.println(stockList);

                // **기간별 기존 데이터만 삭제 후 저장**
                if (rankSortCode == 0) {  // 상승률 저장
                    List<StockRankDomesticRise> riseList = stockList.stream()
                            .map(stock -> (StockRankDomesticRise) stock)
                            .sorted(Comparator.comparing(StockRank::getChangeRate).reversed())
                            .toList();

                    for (int i = 0; i < riseList.size(); i++) {
                        riseList.get(i).setRank(i + 1);
                    }
                    stockRankDomesticRiseRepository.deleteByPeriod(period);
                    stockRankDomesticRiseRepository.saveAll(riseList);
                } else {  // 하락률 저장
                    List<StockRankDomesticFall> fallList = stockList.stream()
                            .map(stock -> (StockRankDomesticFall) stock)
                            .sorted(Comparator.comparing(StockRank::getChangeRate))
                            .toList();

                    for (int i = 0; i < fallList.size(); i++) {
                        fallList.get(i).setRank(i + 1);
                    }
                    stockRankDomesticFallRepository.deleteByPeriod(period);
                    stockRankDomesticFallRepository.saveAll(fallList);
                }
            }
        }
    }

}