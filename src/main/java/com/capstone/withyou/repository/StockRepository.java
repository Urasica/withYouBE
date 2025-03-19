package com.capstone.withyou.repository;

import com.capstone.withyou.dao.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, String> {
    Optional<Stock> findByStockCode(String stockCode);
    Stock findByStockName(String stockName);
}