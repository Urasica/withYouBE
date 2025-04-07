package com.capstone.withyou.controller;

import com.capstone.withyou.dao.Category;
import com.capstone.withyou.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/recommended-list")
    public ResponseEntity<List<Category>> getRecommendedList() {
        return ResponseEntity.ok(recommendationService.getRecommendedList());
    }

    @GetMapping("/category-list")
    public ResponseEntity<List<Category>> getCategoryList() {
        return ResponseEntity.ok(recommendationService.getCategoryList());
    }

}
