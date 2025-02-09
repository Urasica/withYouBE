package com.capstone.withyou.repository;

import com.capstone.withyou.dao.StockRankDomesticTrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRankDomesticTradeRepository extends JpaRepository<StockRankDomesticTrade, Long> {
    List<StockRankDomesticTrade> findAllByOrderByRankAsc();

}
