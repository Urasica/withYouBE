package com.capstone.withyou.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StockPriceDTO {
    @Schema(description = "기간")
    private String date;

    @Schema(description = "시가")
    private double openPrice;

    @Schema(description = "최고가")
    private double highPrice;

    @Schema(description = "최저가")
    private double lowPrice;

    @Schema(description = "종가")
    private double closePrice;

    @Schema(description = "거래량")
    private long volume;

    public StockPriceDTO(String date, double openPrice, double highPrice, double lowPrice, double closePrice, long volume) {
        this.date = formatDate(date);
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
    }

    private String formatDate(String rawDate) {
        return rawDate.substring(0, 4) + "-" + rawDate.substring(4, 6) + "-" + rawDate.substring(6);
    }
}
