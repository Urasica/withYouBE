package com.capstone.withyou.repository;

import com.capstone.withyou.dao.StockPeriod;
import com.capstone.withyou.dao.StockRankDomesticRise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockRankDomesticRiseRepository extends JpaRepository<StockRankDomesticRise, Long> {
    void deleteByPeriod(StockPeriod period);

    @Query("SELECT s FROM StockRankDomesticRise s WHERE s.period = :period ORDER BY s.rank ASC")
    List<StockRankDomesticRise> findByPeriodOrderByRank(StockPeriod period);
}
