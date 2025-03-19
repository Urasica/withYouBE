package com.capstone.withyou.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter @Setter
public class UserReserveHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String stockCode;
    private String stockName;
    private int quantity;
    private Double targetPrice;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private LocalDate ReserveDate;
    private LocalDate TradeDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
