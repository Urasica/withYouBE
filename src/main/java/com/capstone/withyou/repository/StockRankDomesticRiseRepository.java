package com.capstone.withyou.repository;

import com.capstone.withyou.dao.StockPeriod;
import com.capstone.withyou.dao.StockRankDomesticRise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRankDomesticRiseRepository extends JpaRepository<StockRankDomesticRise, Long> {
    List<StockRankDomesticRise> findByPeriod(StockPeriod period);
    void deleteByPeriod(StockPeriod period);
}
