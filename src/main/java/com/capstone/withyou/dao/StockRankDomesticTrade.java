package com.capstone.withyou.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_rank_domestic_trade")
@Getter
@Setter
public class StockRankDomesticTrade {
    @Id
    private String stockCode;       // 종목 코드
    private Integer rank;           // 순위
    private String stockName;       // 종목명
    private Integer currentPrice;   // 현재가
    private Long prevTradeVolume;   // 전일 거래량 (prdy_vol)
    private Long listingShares;     // 상장 주수 (lstn_stcn)
    private Long avgTradeVolume;    // 평균 거래량 (avrg_vol)
    private Long tradeVolume;       // 거래량
    private Integer changePrice;    // 전일 대비 가격
    private BigDecimal changeRate;  // 전일 대비 등락률
    private BigDecimal tradeAmountTurnover; // 거래대금 회전율 (tr_pbmn_tnrt)
    private Long accumulatedTradeAmount;    // 누적 거래 대금 (acml_tr_pbmn)
}