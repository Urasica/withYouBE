package com.capstone.withyou.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class StockPredictionDTO {
    private String stockName;
    private String predictedResult;
}
