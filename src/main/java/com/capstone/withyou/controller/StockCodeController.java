package com.capstone.withyou.controller;

import com.capstone.withyou.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class StockCodeController {
    private final StockService stockService;

    public StockCodeController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/name")
    public ResponseEntity<Void> getWatchListName() throws IOException, InterruptedException {
        stockService.updateStockData();
        return ResponseEntity.ok().build();
    }
}
