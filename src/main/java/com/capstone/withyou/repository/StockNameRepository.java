package com.capstone.withyou.repository;

import com.capstone.withyou.dao.StockInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockNameRepository extends JpaRepository<StockInfo, Long> {
    StockInfo findByStockCode(String stockCode);
}
