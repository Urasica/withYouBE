package com.capstone.withyou.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter @Getter
public class UserStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stockCode;
    private String stockName;
    private int quantity;
    private Double averagePurchasePrice;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
