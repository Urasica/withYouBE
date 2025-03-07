package com.capstone.withyou.dto;

import com.capstone.withyou.dao.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor
public class UserTradeHistoryDTO {
    private String stockCode;
    private String stockName;
    private LocalDate purchaseDate;
    private Double purchasePrice;
    private int quantity;
    private Double totalAmount;
    private TransactionType transactionType;
}
