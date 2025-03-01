package com.capstone.withyou.dto;

import lombok.Getter;

@Getter
public class StockTradeRequestDTO {
    private String userId;
    private String stockCode;
    private int quantity;
}
