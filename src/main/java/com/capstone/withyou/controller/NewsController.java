package com.capstone.withyou.controller;

import com.capstone.withyou.dto.NewsDTO;
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

    // 뉴스 데이터 조회(예측 결과 포함)
    @GetMapping("/{stockName}")
    public ResponseEntity<List<NewsDTO>> getNews(
            @PathVariable String stockName,
            @RequestParam(defaultValue = "주식 주가") String category,
            @RequestParam(defaultValue = "1") int page
    ) {
        List<NewsDTO> news = newsService.getNewsAndPrediction(stockName, category, page);
        return ResponseEntity.ok(news);
    }
}
