package com.capstone.withyou.repository;

import com.capstone.withyou.dao.StockPeriod;
import com.capstone.withyou.dao.StockRankDomesticFall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockRankDomesticFallRepository extends JpaRepository<StockRankDomesticFall, Long> {
    void deleteByPeriod(StockPeriod period);

    @Query("SELECT s FROM StockRankDomesticFall s WHERE s.period = :period ORDER BY s.rank ASC")
    List<StockRankDomesticFall> findByPeriodOrderByRank(StockPeriod period);

    @Query("SELECT s FROM StockRankDomesticFall s WHERE s.period = 'DAILY' AND s.rank = :rank")
    StockRankDomesticFall findByRank(int rank);
}
