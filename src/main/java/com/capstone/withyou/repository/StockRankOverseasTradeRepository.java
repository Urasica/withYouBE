package com.capstone.withyou.repository;

import com.capstone.withyou.dao.StockRankOverseasTrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRankOverseasTradeRepository extends JpaRepository<StockRankOverseasTrade, Long> {
    List<StockRankOverseasTrade> findAllByOrderByRankAsc();
}
