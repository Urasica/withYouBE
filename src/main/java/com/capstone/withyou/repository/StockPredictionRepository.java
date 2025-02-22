package com.capstone.withyou.repository;

import com.capstone.withyou.dao.StockPrediction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPredictionRepository extends JpaRepository<StockPrediction, Long> {
    StockPrediction findTopByStockName(String stockName);
    void deleteByStockName(String stockName);
}
