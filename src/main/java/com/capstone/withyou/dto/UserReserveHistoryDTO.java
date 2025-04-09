package com.capstone.withyou.dto;

import com.capstone.withyou.dao.TransactionStatus;
import com.capstone.withyou.dao.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor
public class UserReserveHistoryDTO {
    private Long id;
    private String stockCode;
    private String stockName;
    private int quantity;
    private Double targetPrice;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private LocalDate reserveDate;
    private LocalDate tradeDate;
}
