package com.capstone.withyou.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StockPriceDayDTO {
    @Schema(description = "한국 기준 일자")
    private String date;

    @Schema(description = "한국 기준 시간")
    private String time;

    @Schema(description = "시가")
    private double openPrice;

    @Schema(description = "최고가")
    private double highPrice;

    @Schema(description = "최저가")
    private double lowPrice;

    @Schema(description = "종가")
    private double closePrice;

    @Schema(description = "체결량")
    private long volume;

    @Schema(description = "체결대금")
    private long amountVolume;

    public StockPriceDayDTO(String date, String time, double openPrice, double highPrice, double lowPrice, double closePrice, long volume, long amountVolume) {
        this.date = formatDate(date);
        this.time = formatTime(time);
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
        this.amountVolume = amountVolume;
    }

    private String formatDate(String rawDate) {
        return rawDate.substring(0, 4) + "-" + rawDate.substring(4, 6) + "-" + rawDate.substring(6);
    }

    private String formatTime(String rawTime) {
        return rawTime.substring(0, 2) + ":" + rawTime.substring(2, 4) + ":" + rawTime.substring(4);
    }
}
