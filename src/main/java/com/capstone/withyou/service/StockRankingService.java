package com.capstone.withyou.service;

import com.capstone.withyou.Manager.AccessTokenManager;
import com.capstone.withyou.dao.*;
import com.capstone.withyou.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final StockRankDomesticTradeRepository stockRankDomesticTradeRepository;
    private final StockRankOverseasRiseRepository stockRankOverseasRiseRepository;
    private final StockRankOverseasFallRepository stockRankOverseasFallRepository;
    private final StockRankOverseasTradeRepository stockRankOverseasTradeRepository;


    private final AccessTokenManager tokenManager;
    private static final int API_CALL_INTERVAL = 1000;


    public StockRankingService(StockRankDomesticRiseRepository stockRankDomesticRiseRepository,
                               AccessTokenManager tokenManager,
                               StockRankDomesticFallRepository stockRankDomesticFallRepository,
                               StockRankDomesticTradeRepository stockRankDomesticTradeRepository,
                               StockRankOverseasRiseRepository stockRankOverseasRiseRepository,
                               StockRankOverseasFallRepository stockRankOverseasFallRepository,
                               StockRankOverseasTradeRepository stockRankOverseasTradeRepository) {
        this.stockRankDomesticRiseRepository = stockRankDomesticRiseRepository;
        this.tokenManager = tokenManager;
        this.stockRankDomesticFallRepository = stockRankDomesticFallRepository;
        this.stockRankDomesticTradeRepository = stockRankDomesticTradeRepository;
        this.stockRankOverseasRiseRepository = stockRankOverseasRiseRepository;
        this.stockRankOverseasFallRepository = stockRankOverseasFallRepository;
        this.stockRankOverseasTradeRepository = stockRankOverseasTradeRepository;
    }

    @Transactional
    public void fetchAndSaveDailyStockRankings() {
        try {
            DomesticStockRankingChangeRate(0, StockPeriod.DAILY);
            TimeUnit.MILLISECONDS.sleep(API_CALL_INTERVAL);
            DomesticStockRankingChangeRate(1, StockPeriod.DAILY);
            TimeUnit.MILLISECONDS.sleep(API_CALL_INTERVAL);
            DomesticStockRankingTradeVolume();
            TimeUnit.MILLISECONDS.sleep(API_CALL_INTERVAL);

            OverseasStockRanking(0, StockPeriod.DAILY);
            TimeUnit.MILLISECONDS.sleep(API_CALL_INTERVAL);
            OverseasStockRanking(1, StockPeriod.DAILY);
            TimeUnit.MILLISECONDS.sleep(API_CALL_INTERVAL);
            OverseasStockTradeRanking();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public void fetchAndSaveLongTermStockRankings() {
        try {
            for (StockPeriod period : Arrays.asList(StockPeriod.WEEKLY, StockPeriod.MONTHLY, StockPeriod.YEARLY)) {
                DomesticStockRankingChangeRate(0, period);
                TimeUnit.MILLISECONDS.sleep(API_CALL_INTERVAL);
                DomesticStockRankingChangeRate(1, period);
                TimeUnit.MILLISECONDS.sleep(API_CALL_INTERVAL);
                OverseasStockRanking(0, period);
                TimeUnit.MILLISECONDS.sleep(API_CALL_INTERVAL);
                OverseasStockRanking(1, period);
                TimeUnit.MILLISECONDS.sleep(API_CALL_INTERVAL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 국내 주식 등락률 순위 (0:상승률, 1:하락률)
     */
    @Transactional
    public void DomesticStockRankingChangeRate(int rankSortCode, StockPeriod period) {
        String url = "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/ranking/fluctuation"
                + "?fid_cond_mrkt_div_code=J"
                + "&fid_cond_scr_div_code=20170"
                + "&fid_input_iscd=0000"
                + "&fid_rank_sort_cls_code=" + rankSortCode /*순위정렬 구분코드 0:상승율순 1:하락율순*/
                + "&fid_input_cnt_1=" + period.getDays() /*누적일*/
                + "&fid_prc_cls_code=1" /*가격 구분코드 1:종가대비 */
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
        headers.set("tr_cont", "");
        headers.set("custtype", "P");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        JsonNode responseBody = response.getBody();

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

    /**
     * 국내 주식 거래량 순위
     */
    @Transactional
    public void DomesticStockRankingTradeVolume() {
        String url = "https://openapi.koreainvestment.com:9443//uapi/domestic-stock/v1/quotations/volume-rank"
                + "?FID_COND_MRKT_DIV_CODE=J"
                + "&FID_COND_SCR_DIV_CODE=20171"
                + "&FID_INPUT_ISCD=0000"
                + "&FID_DIV_CLS_CODE=0"
                + "&FID_BLNG_CLS_CODE=0"
                + "&FID_TRGT_CLS_CODE=111111111"
                + "&FID_TRGT_EXLS_CLS_CODE=1011011111"
                + "&FID_INPUT_PRICE_1="
                + "&FID_INPUT_PRICE_2="
                + "&FID_VOL_CNT="
                + "&FID_INPUT_DATE_1=";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + tokenManager.getAccessToken());
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", "FHPST01710000");
        headers.set("tr_cont", "");
        headers.set("custtype", "P");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        JsonNode responseBody = response.getBody();

        if (responseBody != null) {
            JsonNode rankings = responseBody.get("output");

            if (rankings != null) {
                List<StockRankDomesticTrade> stockList = new ArrayList<>();
                for (JsonNode ranking : rankings) {
                    StockRankDomesticTrade stock = new StockRankDomesticTrade();
                    stock.setStockCode(ranking.get("mksc_shrn_iscd").asText());
                    stock.setRank(ranking.get("data_rank").asInt());
                    stock.setStockName(ranking.get("hts_kor_isnm").asText());
                    stock.setCurrentPrice(ranking.get("stck_prpr").asInt());
                    stock.setChangePrice(ranking.get("prdy_vrss").asInt());
                    stock.setChangeRate(new BigDecimal(ranking.get("prdy_ctrt").asText()));
                    stock.setTradeVolume(ranking.get("acml_vol").asLong());
                    stock.setPrevTradeVolume(ranking.get("prdy_vol").asLong());
                    stock.setListingShares(ranking.get("lstn_stcn").asLong());
                    stock.setAvgTradeVolume(ranking.get("avrg_vol").asLong());
                    stock.setTradeAmountTurnover(new BigDecimal(ranking.get("tr_pbmn_tnrt").asText()));
                    stock.setAccumulatedTradeAmount(ranking.get("acml_tr_pbmn").asLong());

                    stockList.add(stock);
                }

                stockRankDomesticTradeRepository.deleteAll();
                stockRankDomesticTradeRepository.saveAll(stockList);
            }
        }
    }

    /**
     * 해외 주식 등락률 순위 (0:하락률, 1:상승률)
     */
    @Transactional
    public void OverseasStockRanking(int rankSortCode, StockPeriod period) {
        int dayCode = switch (period) {
            case DAILY -> 0;   // 당일
            case WEEKLY -> 3;  // 5일
            case MONTHLY -> 6; // 30일
            case YEARLY -> 9;  // 1년
        };

        String url = "https://openapi.koreainvestment.com:9443/uapi/overseas-stock/v1/ranking/updown-rate"
                + "?AUTH="
                + "&EXCD=NAS"
                + "&GUBN=" + rankSortCode /*순위정렬 구분코드 1:상승율순 0:하락율순*/
                + "&NDAY=" + dayCode /*누적일*/
                + "&VOL_RANG=1"
                + "&KEYB=";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + tokenManager.getAccessToken());
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", "HHDFS76290000");
        headers.set("tr_cont", "");
        headers.set("custtype", "P");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        JsonNode responseBody = response.getBody();

        if (responseBody != null) {
            JsonNode rankings = responseBody.get("output2");

            if (rankings != null) {
                List<StockRankOverseas> stockList = new ArrayList<>();
                for (JsonNode ranking : rankings) {
                    StockRankOverseas stock = rankSortCode==0 ? new StockRankOverseasFall() : new StockRankOverseasRise();
                    stock.setStockCode(ranking.get("symb").asText());
                    stock.setRank(ranking.get("rank").asInt());
                    stock.setStockName(ranking.get("name").asText());
                    stock.setStockNameEng(ranking.get("ename").asText());
                    stock.setCurrentPrice(new BigDecimal(ranking.get("last").asText()).doubleValue());
                    stock.setChangePrice(new BigDecimal(ranking.get("diff").asText()).doubleValue());
                    stock.setChangeRate(new BigDecimal(ranking.get("rate").asText().trim().replaceAll("\\s+", "")));
                    stock.setTradeVolume(ranking.get("tvol").asLong());
                    stock.setExcd(ranking.get("excd").asText());
                    stock.setPeriod(period);

                    stockList.add(stock);
                }

                if(rankSortCode == 0) { // 하락률 저장
                    List<StockRankOverseasFall> list = stockList.stream()
                            .map(stock -> (StockRankOverseasFall) stock)
                            .toList();

                    stockRankOverseasFallRepository.deleteByPeriod(period);
                    stockRankOverseasFallRepository.saveAll(list);
                } else { // 상승률 저장
                    List<StockRankOverseasRise> list = stockList.stream()
                            .map(stock -> (StockRankOverseasRise) stock)
                            .toList();

                    stockRankOverseasRiseRepository.deleteByPeriod(period);
                    stockRankOverseasRiseRepository.saveAll(list);
                }
            }
        }
    }

    /**
     * 해외 주식 거래량 순위
     */
    @Transactional
    public void OverseasStockTradeRanking() {
        String url = "https://openapi.koreainvestment.com:9443/uapi/overseas-stock/v1/ranking/updown-rate"
                + "?AUTH="
                + "&EXCD=NAS"
                + "&NDAY=0"
                + "&PRC1="
                + "&PRC2="
                + "&VOL_RANG=1"
                + "&KEYB=";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + tokenManager.getAccessToken());
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", "HHDFS76310010");
        headers.set("tr_cont", "");
        headers.set("custtype", "P");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        JsonNode responseBody = response.getBody();

        if (responseBody != null) {
            JsonNode rankings = responseBody.get("output2");

            if (rankings != null) {
                List<StockRankOverseasTrade> stockList = new ArrayList<>();
                for (JsonNode ranking : rankings) {
                    StockRankOverseasTrade stock = new StockRankOverseasTrade();
                    stock.setStockCode(ranking.get("symb").asText());
                    stock.setRank(ranking.get("rank").asInt());
                    stock.setStockName(ranking.get("name").asText());
                    stock.setStockNameEng(ranking.get("ename").asText());
                    stock.setCurrentPrice(new BigDecimal(ranking.get("last").asText()).doubleValue());
                    stock.setChangePrice(new BigDecimal(ranking.get("diff").asText()).doubleValue());
                    stock.setChangeRate(new BigDecimal(ranking.get("rate").asText().trim().replaceAll("\\s+", "")));
                    stock.setTradeVolume(ranking.get("tvol").asLong());
                    stock.setExcd(ranking.get("excd").asText());

                    stockList.add(stock);
                }

                stockRankOverseasTradeRepository.deleteAll();
                stockRankOverseasTradeRepository.saveAll(stockList);
            }
        }
    }
}