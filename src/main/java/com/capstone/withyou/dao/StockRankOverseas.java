package com.capstone.withyou.dao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@MappedSuperclass
@IdClass(StockRankId.class)
public abstract class StockRankOverseas {
    @Id
    @Schema(description = "종목 코드 (ex: GLDD)")
    private String stockCode;   // 종목 코드

    @Id
    @Enumerated(EnumType.STRING)
    @Schema(description = "기간")
    private StockPeriod period;     // 기간 (1일, 1주, 1달, 1년)

    @Schema(description = "순위 (1위부터 시작)")
    private Integer rank;            // 순위

    @Schema(description = "거래소 코드 (ex: NAS = 나스닥)")
    private String excd;             // 거래소 코드

    @Schema(description = "종목명 한글")
    private String stockName;        // 종목명

    @Schema(description = "종목명 영문")
    private String stockNameEng;     // 영문 종목명 (ename)

    @Schema(description = "현재 가격 (ex: 7.2562 = 7.2562달러)")
    private Double currentPrice;     // 현재가

    @Schema(description = "전일 대비 가격 변화량 (ex: 0.232 = 0.232달러)")
    private Double changePrice;      // 전일 대비 가격

    @Schema(description = "등락률 (%)")
    private BigDecimal changeRate;   // 전일 대비 등락률

    @Schema(description = "거래량")
    private Long tradeVolume;        // 거래량
}
