package com.capstone.withyou.controller;

import com.capstone.withyou.dto.NewsDTO;
import com.capstone.withyou.dto.StockPredictionDTO;
import com.capstone.withyou.service.NewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/news")
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    // 뉴스 데이터 조회
    @GetMapping("/{stockName}")
    public ResponseEntity<List<NewsDTO>> getNews(
            @PathVariable String stockName,
            @RequestParam(defaultValue = "주식 주가") String category,
            @RequestParam(defaultValue = "1") int page
    ) {
        List<NewsDTO> news = newsService.getNews(stockName, category, page);
        return ResponseEntity.ok(news);
    }

    // 사용자 종목별 대표 뉴스 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NewsDTO>> getUserStockNews(
            @PathVariable String userId,
            @RequestParam(defaultValue = "주식 주가") String category) {
        List<NewsDTO> newsList = newsService.getUserStockNews(userId, category);
        return ResponseEntity.ok(newsList);
    }

    // 예측 결과 조회
    @GetMapping("/{stockName}/prediction")
    public ResponseEntity<StockPredictionDTO> getPrediction(@PathVariable String stockName){
        StockPredictionDTO prediction = newsService.getPrediction(stockName);
        return ResponseEntity.ok(prediction);
    }
}
