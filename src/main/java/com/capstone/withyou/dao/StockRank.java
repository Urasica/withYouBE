package com.capstone.withyou.dao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@MappedSuperclass
@IdClass(StockRankId.class)  // 복합키 사용
public abstract class StockRank {
    @Id
    @Schema(description = "종목 코드 (ex: 083660)")
    private String stockCode;       // 종목 코드
    @Id
    @Enumerated(EnumType.STRING)
    @Schema(description = "기간")
    private StockPeriod period;     // 기간 (1일, 1주, 1달, 1년)

    @Schema(description = "순위 (1위부터 시작)")
    private Integer rank;           // 순위

    @Schema(description = "종목명")
    private String stockName;       // 종목명

    @Schema(description = "현재 가격")
    private Integer currentPrice;   // 현재가

    @Schema(description = "전일 대비 가격 변화량")
    private Integer changePrice;    // 전일 대비 가격

    @Schema(description = "등락률 (%)")
    private BigDecimal changeRate;  // 전일 대비 등락률

    @Schema(description = "거래량")
    private Long tradeVolume;       // 거래량

    @Schema(description = "당일 최고가")
    private Integer highestPrice;   // 최고가

    @Schema(description = "당일 최저가")
    private Integer lowestPrice;    // 최저가
}
