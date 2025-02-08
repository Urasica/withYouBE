package com.capstone.withyou.repository;

import com.capstone.withyou.dao.StockPeriod;
import com.capstone.withyou.dao.StockRankOverseasRise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRankOverseasRiseRepository extends JpaRepository<StockRankOverseasRise, Long> {
    List<StockRankOverseasRise> findByPeriod(StockPeriod period);
}
