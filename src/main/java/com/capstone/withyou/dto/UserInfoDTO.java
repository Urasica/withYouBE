package com.capstone.withyou.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO {
    private String userId;
    private Double balance; //보유 금액
    private Double totalPurchase; //총 매입
    private Double totalEvaluation; //총 평가
    private Double totalProfit; //총 수익
    private Double totalProfitRate; //총 수익률
    private Double profitGoal; //목표 수익률
    private List<UserStockDTO> stocks;
}
