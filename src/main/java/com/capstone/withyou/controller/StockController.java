package com.capstone.withyou.controller;

import com.capstone.withyou.dto.StockDTO;
import com.capstone.withyou.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    // 주식 리스트 조회(검색 기능)
    @GetMapping("/stock-list")
    public ResponseEntity<List<StockDTO>> getStockList(){
        stockService.getStocks();
        return ResponseEntity.ok(stockService.getStocks());
    }
}
