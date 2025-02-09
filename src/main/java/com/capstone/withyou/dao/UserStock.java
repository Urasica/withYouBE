package com.capstone.withyou.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Setter @Getter
public class UserStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stockCode;
    private int quantity;
    private BigDecimal purchasePrice;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
