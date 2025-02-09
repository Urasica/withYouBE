package com.capstone.withyou.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO {
    private String userId;
    private BigDecimal balance; //보유 금액
    private BigDecimal totalPurchase; //총 매입
    private BigDecimal totalEvaluation; //총 평가
    private BigDecimal totalProfit; //총 수익
    private BigDecimal totalProfitRate; //총 수익률
    private BigDecimal realizedProfit; //실현 손익
    private List<UserStockDTO> stocks;
}
