package com.capstone.withyou.repository;

import com.capstone.withyou.dao.StockPeriod;
import com.capstone.withyou.dao.StockRankOverseasFall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRankOverseasFallRepository extends JpaRepository<StockRankOverseasFall, Long> {
    List<StockRankOverseasFall> findByPeriod(StockPeriod period);
}
