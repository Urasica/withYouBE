package com.capstone.withyou.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfitDTO {
    private String userId;
    private Double totalProfit;
}
