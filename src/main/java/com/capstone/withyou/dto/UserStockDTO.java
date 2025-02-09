package com.capstone.withyou.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserStockDTO {
    private String stockCode;
    private BigDecimal currentPrice; // 현재 주가
    private BigDecimal purchasePrice; // 매입 주가
    private BigDecimal profitAmount; //손익 금액
    private BigDecimal profitRate; //손익률
    private BigDecimal totalAmount; //총 금액(현재 주가 * 주식 수)
    private int quantity; //주식 수
}
