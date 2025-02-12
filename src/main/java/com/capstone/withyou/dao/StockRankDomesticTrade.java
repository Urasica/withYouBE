package com.capstone.withyou.dao;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "종목 코드 (예: 005930)")
    private String stockCode;

    @Schema(description = "순위 (1위부터 시작)")
    private Integer rank;

    @Schema(description = "종목명 (예: 삼성전자)")
    private String stockName;

    @Schema(description = "현재가")
    private Integer currentPrice;

    @Schema(description = "전일 거래량")
    private Long prevTradeVolume;

    @Schema(description = "상장 주수")
    private Long listingShares;

    @Schema(description = "평균 거래량")
    private Long avgTradeVolume;

    @Schema(description = "거래량")
    private Long tradeVolume;

    @Schema(description = "전일 대비 가격 변동")
    private Integer changePrice;

    @Schema(description = "전일 대비 등락률 (%)")
    private BigDecimal changeRate;

    @Schema(description = "거래대금 회전율")
    private BigDecimal tradeAmountTurnover;

    @Schema(description = "누적 거래 대금")
    private Long accumulatedTradeAmount;
}