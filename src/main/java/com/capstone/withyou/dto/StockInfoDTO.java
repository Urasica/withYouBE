package com.capstone.withyou.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockInfoDTO {
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
}
