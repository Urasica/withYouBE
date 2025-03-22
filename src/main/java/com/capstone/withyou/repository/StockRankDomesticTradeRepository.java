package com.capstone.withyou.repository;

import com.capstone.withyou.dao.StockRankDomesticTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockRankDomesticTradeRepository extends JpaRepository<StockRankDomesticTrade, Long> {
    List<StockRankDomesticTrade> findAllByOrderByRankAsc();

    @Query("SELECT s FROM StockRankDomesticTrade s WHERE s.rank = :rank")
    StockRankDomesticTrade findByRank(int rank);
}
