package com.capstone.withyou.repository;

import com.capstone.withyou.dao.StockInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockInfoRepository extends JpaRepository<StockInfo, Long> {
    Optional<StockInfo> findByStockCode(String stockCode);
}
