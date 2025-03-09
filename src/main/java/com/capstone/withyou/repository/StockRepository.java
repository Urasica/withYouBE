package com.capstone.withyou.repository;

import com.capstone.withyou.dao.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, String> {
    Stock findByStockCode(String stockCode);
    Stock findByStockName(String stockName);
}