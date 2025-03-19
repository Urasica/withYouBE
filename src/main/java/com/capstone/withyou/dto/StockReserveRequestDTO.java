package com.capstone.withyou.dto;

import lombok.Getter;

@Getter
public class StockReserveRequestDTO {
    private String userId;
    private String stockCode;
    private int quantity;
    private double targetPrice; //예약 가격
}
