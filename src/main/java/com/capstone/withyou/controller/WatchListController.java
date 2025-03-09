package com.capstone.withyou.controller;

import com.capstone.withyou.dto.WatchListDTO;
import com.capstone.withyou.dto.WatchListStockPriceDTO;
import com.capstone.withyou.service.StockPriceService;
import com.capstone.withyou.service.StockService;
import com.capstone.withyou.service.WatchListService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/watchlist")
public class WatchListController {

    private final WatchListService watchListService;
    private final StockPriceService stockPriceService;
    private final StockService stockService;

    public WatchListController(WatchListService watchListService,
                               StockPriceService stockPriceService,
                               StockService stockService) {
        this.watchListService = watchListService;
        this.stockPriceService = stockPriceService;
        this.stockService = stockService;
    }

    // 관심 등록
    @PostMapping("/add")
    public ResponseEntity<String> addToWatchList(@RequestBody WatchListDTO request) {
        watchListService.addToWatchList(request.getUserId(), request.getStockCode());
        return ResponseEntity.ok("Stock added to watch list");
    }

    // 관심 해제
    @PostMapping("/remove")
    public ResponseEntity<String> removeFromWatchList(@RequestBody WatchListDTO request) {
        watchListService.removeFromWatchList(request.getUserId(), request.getStockCode());
        return ResponseEntity.ok("Stock removed from watch list");
    }

    // 관심 목록 조회
    @GetMapping("/{userId}")
    public ResponseEntity<List<WatchListStockPriceDTO>> getWatchList(@PathVariable String userId) {
        List<String> stocks = watchListService.getWatchList(userId);
        List<WatchListStockPriceDTO> stockPrices = new ArrayList<>();
        for (String stockCode : stocks) {
            if (stockCode.chars().allMatch(Character::isDigit)) {
                WatchListStockPriceDTO stock = stockPriceService.getDomesticWatchListStockCurPrice(stockCode);
                stock.setStockName(stockService.getStockName(stockCode));
                stockPrices.add(stock);
            } else if (stockCode.chars().allMatch(Character::isLetterOrDigit)) {
                WatchListStockPriceDTO stock = stockPriceService.getOverseasStockWatchListStockCurPrice(stockCode);
                stock.setStockName(stockService.getStockName(stockCode));
                stockPrices.add(stock);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid stock code");
            }
        }
        return ResponseEntity.ok(stockPrices);
    }
}
