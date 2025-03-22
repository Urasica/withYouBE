package com.capstone.withyou.repository;

import com.capstone.withyou.dao.StockPeriod;
import com.capstone.withyou.dao.StockRankOverseasRise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockRankOverseasRiseRepository extends JpaRepository<StockRankOverseasRise, Long> {
    void deleteByPeriod(StockPeriod period);

    @Query("SELECT s FROM StockRankOverseasRise s WHERE s.period = :period ORDER BY s.rank ASC")
    List<StockRankOverseasRise> findByPeriodOrderByRank(StockPeriod period);

    @Query("SELECT s FROM StockRankOverseasRise s WHERE s.period = 'DAILY' AND s.rank = :rank")
    StockRankOverseasRise findByRank(int rank);

}
