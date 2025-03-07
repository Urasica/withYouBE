package com.capstone.withyou.repository;

import com.capstone.withyou.dao.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockNameRepository extends JpaRepository<Stock, Long> {
    Stock findByStockCode(String stockCode);
}
