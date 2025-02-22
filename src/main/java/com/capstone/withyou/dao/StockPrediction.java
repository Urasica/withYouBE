package com.capstone.withyou.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class StockPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String stockName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String predictedResult;

    public StockPrediction(String stockName, String predictedResult) {
        this.stockName = stockName;
        this.predictedResult = predictedResult;
    }
}
