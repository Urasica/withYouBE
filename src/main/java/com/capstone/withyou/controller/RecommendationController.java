package com.capstone.withyou.controller;

import com.capstone.withyou.dao.Category;
import com.capstone.withyou.dto.StockDTO;
import com.capstone.withyou.service.RecommendationService;
import com.capstone.withyou.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final StockService stockService;

    // 추천 카테고리 조회
    @GetMapping("/recommended-list")
    public ResponseEntity<List<Category>> getRecommendedList() {
        return ResponseEntity.ok(recommendationService.getRecommendedList());
    }

    // 모든 카테고리 조회
    @GetMapping("/category-list")
    public ResponseEntity<List<Category>> getCategoryList() {
        return ResponseEntity.ok(recommendationService.getCategoryList());
    }

    // 카테고리별 주식 리스트 조회
    @GetMapping("/category/{categoryName}/stock-list")
    public ResponseEntity<List<StockDTO>> getCategoryStockList(@PathVariable String categoryName) {
        return ResponseEntity.ok(stockService.getCategoryStocks(categoryName));
    }

    @GetMapping("/recommended")
    @Operation(
            summary = "테마주 추천 수동 갱신(사용 금지)",
            description = "사용 금지")
    public ResponseEntity<Void> updateRecommended() {
        recommendationService.updateCategoryDeviations();
        return ResponseEntity.ok().build();
    }
}
