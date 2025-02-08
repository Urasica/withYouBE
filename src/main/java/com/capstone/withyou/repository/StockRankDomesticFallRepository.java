package com.capstone.withyou.repository;

import com.capstone.withyou.dao.StockPeriod;
import com.capstone.withyou.dao.StockRankDomesticFall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRankDomesticFallRepository extends JpaRepository<StockRankDomesticFall, Long> {
    List<StockRankDomesticFall> findByPeriod(StockPeriod period);
    void deleteByPeriod(StockPeriod period);
}
