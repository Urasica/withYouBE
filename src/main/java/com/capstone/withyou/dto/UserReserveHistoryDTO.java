package com.capstone.withyou.dto;

import com.capstone.withyou.dao.TransactionStatus;
import com.capstone.withyou.dao.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter @Setter
public class UserReserveHistoryDTO {
    private String stockCode;
    private String stockName;
    private int quantity;
    private Double targetPrice;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private LocalDate ReserveDate;
    private LocalDate TradeDate;
}
