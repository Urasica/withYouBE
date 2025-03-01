package com.capstone.withyou.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter @Setter
public class UserTradeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String stockCode;
    private String stockName;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private int quantity;
    private BigDecimal totalAmount;
    private TransactionType transactionType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
