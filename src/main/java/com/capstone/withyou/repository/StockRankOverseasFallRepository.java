package com.capstone.withyou.repository;

import com.capstone.withyou.dao.StockPeriod;
import com.capstone.withyou.dao.StockRankOverseasFall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockRankOverseasFallRepository extends JpaRepository<StockRankOverseasFall, Long> {
    void deleteByPeriod(StockPeriod period);

    @Query("SELECT s FROM StockRankOverseasFall s WHERE s.period = :period ORDER BY s.rank ASC")
    List<StockRankOverseasFall> findByPeriodOrderByRank(StockPeriod period);

    @Query("SELECT s FROM StockRankOverseasFall s WHERE s.period = 'DAILY' AND s.rank = :rank")
    StockRankOverseasFall findByRank(int rank);
}
