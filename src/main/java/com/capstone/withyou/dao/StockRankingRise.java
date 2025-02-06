package com.capstone.withyou.dao;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@Entity
public class StockRankingRise {
    @Id
    private String stockCode;       // 종목 코드
    private Integer rank;           // 순위
    private String stockName;       // 종목명
    private Integer currentPrice;   // 현재가
    private Integer changePrice;    // 전일 대비 가격
    private BigDecimal changeRate;  // 전일 대비 등락률
    private Long tradeVolume;       // 거래량
    private Integer highestPrice;   // 최고가
    private Integer lowestPrice;    // 최저가
}
