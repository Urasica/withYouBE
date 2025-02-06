package com.capstone.withyou.controller;

import com.capstone.withyou.Manager.AccessTokenManager;
import com.capstone.withyou.dao.StockRankingRise;
import com.capstone.withyou.repository.StockRankingRiseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/api/stock")
public class StockRankListController {
    private final AccessTokenManager accessTokenManager;
    private final StockRankingRiseRepository stockRankingRiseRepository;

    @Autowired
    public StockRankListController(AccessTokenManager accessTokenManager, StockRankingRiseRepository stockRankingRiseRepository) {
        this.accessTokenManager = accessTokenManager;
        this.stockRankingRiseRepository = stockRankingRiseRepository;
    }

    @GetMapping("/test")
    public String testToken() {
        if (accessTokenManager.getAccessToken() == null) { return "fail";}
        return "success";
    }

    @GetMapping("/rise")
    public ResponseEntity<List<StockRankingRise>> getAllRisingStocks() {
        List<StockRankingRise> stocks = stockRankingRiseRepository.findAll();
        return ResponseEntity.ok(stocks);
    }
}
