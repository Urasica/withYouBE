package com.capstone.withyou.repository;

import com.capstone.withyou.dao.StockRankOverseasTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockRankOverseasTradeRepository extends JpaRepository<StockRankOverseasTrade, Long> {
    List<StockRankOverseasTrade> findAllByOrderByRankAsc();

    @Query("SELECT s FROM StockRankOverseasTrade s WHERE s.rank = :rank")
    StockRankOverseasTrade findByRank(int rank);
}
