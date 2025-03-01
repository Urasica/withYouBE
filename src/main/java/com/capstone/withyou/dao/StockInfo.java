package com.capstone.withyou.dao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(name = "stock_info")
@Schema(description = "주식 종목 정보")
@NoArgsConstructor
public class StockInfo {
    @Id
    @Schema(description = "주식 코드")
    private String stockCode;

    @Schema(description = "전일 가격")
    private String pdpr;

    @Schema(description = "시가")
    private String oppr;

    @Schema(description = "고가")
    private String hypr;

    @Schema(description = "저가")
    private String lopr;

    @Schema(description = "거래량")
    private String tvol;

    @Schema(description = "거래 대금")
    private String tamt;

    @Schema(description = "시가총액")
    private String tomv;

    @Schema(description = "52주 최고")
    private String h52p;

    @Schema(description = "52주 최저")
    private String l52p;

    @Schema(description = "PER")
    private String per;

    @Schema(description = "PBR")
    private String pbr;

    @Schema(description = "EPS")
    private String eps;

    @Schema(description = "BPS")
    private String bps;

    private LocalDateTime lastUpdated;
}

