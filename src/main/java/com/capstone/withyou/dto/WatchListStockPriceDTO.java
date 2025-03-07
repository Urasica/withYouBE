package com.capstone.withyou.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WatchListStockPriceDTO {
    @Schema(description = "주식코드")
    String stockCode;
    @Schema(description = "주식명")
    String stockName;
    @Schema(description = "현재가")
    String stockCurrentPrice;
    @Schema(description = "전일 대비")
    String stockChange;
    @Schema(description = "전일 대비율")
    String stockChangePercent;
    @Schema(description = "누적 거래량")
    String acml_vol;
    @Schema(description = "누적 거래대금")
    String acml_tr_pbmn;
}
