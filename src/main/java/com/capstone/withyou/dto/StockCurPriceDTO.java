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
public class StockCurPriceDTO {
    @Schema(description = "주식 현재가")
    private double StockPrice;

    @Schema(description = "전일 대비 가격")
    private double changePrice;

    @Schema(description = "전일 대비율")
    private double changeRate;
}
