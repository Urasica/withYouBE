package com.capstone.withyou.controller;

import com.capstone.withyou.Manager.AccessTokenManager;
import com.capstone.withyou.dao.StockRankDomesticRise;
import com.capstone.withyou.repository.StockRankDomesticRiseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/api/stock")
public class StockRankListController {
    private final AccessTokenManager accessTokenManager;
    private final StockRankDomesticRiseRepository stockRankDomesticRiseRepository;

    @Autowired
    public StockRankListController(AccessTokenManager accessTokenManager,
                                   StockRankDomesticRiseRepository stockRankDomesticRiseRepository) {
        this.accessTokenManager = accessTokenManager;
        this.stockRankDomesticRiseRepository = stockRankDomesticRiseRepository;
    }

    @GetMapping("/test")
    public String testToken() {
        if (accessTokenManager.getAccessToken() == null) { return "fail";}
        return "success";
    }

    @GetMapping("/rise")
    public ResponseEntity<List<StockRankDomesticRise>> getAllRisingStocks() {
        List<StockRankDomesticRise> stocks = stockRankDomesticRiseRepository.findAll();
        return ResponseEntity.ok(stocks);
    }
}
