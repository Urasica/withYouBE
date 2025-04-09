package com.capstone.withyou.repository;

import com.capstone.withyou.dao.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, String> {
    Optional<Stock> findByStockCode(String stockCode);
    Stock findByStockName(String stockName);

    @Query(value = "SELECT * FROM stock s WHERE s.stock_code REGEXP '^[A-Za-z]' ", nativeQuery = true)
    List<Stock> findNASDAQStocks();

}