package com.capstone.withyou.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserStockDTO {
    private String stockCode;
    private String stockName;
    private Double currentPrice; // 현재 주가
    private Double averagePurchasePrice; // 평균 매입 주가
    private Double profitAmount; //손익 금액
    private Double profitRate; //손익률
    private Double totalAmount; //총 금액(현재 주가 * 주식 수)
    private int quantity; //주식 수
}
