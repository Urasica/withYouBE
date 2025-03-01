package com.capstone.withyou.controller;

import com.capstone.withyou.dto.WatchListDTO;
import com.capstone.withyou.service.WatchListService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/watchlist")
public class WatchListController {

    private final WatchListService watchListService;

    public WatchListController(WatchListService watchListService) {
        this.watchListService = watchListService;
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
    public ResponseEntity<List<String>> getWatchList(@PathVariable String userId) {
        return ResponseEntity.ok(watchListService.getWatchList(userId));
    }
}
