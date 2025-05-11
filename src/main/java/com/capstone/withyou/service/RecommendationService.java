package com.capstone.withyou.service;

import com.capstone.withyou.dao.Category;
import com.capstone.withyou.dao.Stock;
import com.capstone.withyou.repository.CategoryRepository;
import com.capstone.withyou.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecommendationService {

    private final StockRepository stockRepository;
    private final PythonScriptService pythonScriptService;
    private final CategoryRepository categoryRepository;

    // 카테고리 분류해서 수치 높은 5개, 낮은 5개 반환
    @Transactional
    public void updateCategoryDeviations() {
        // 주식 리스트 가져오기
        List<Stock> nasdaqStocks = stockRepository.findNASDAQStocks();

        try {
            // 모든 주식 수치 계산
            Map<String, Double> deviations = pythonScriptService.calculateDeviations();

            // 카테고리 업데이트
            for (Stock stock : nasdaqStocks) {
                Double deviation = deviations.get(stock.getStockCode());

                if (deviation == null || Double.isNaN(deviation)) {
                    continue; // 편차 계산 실패 시 건너뛰기
                }

                Optional<Stock> optionalStock = stockRepository.findByStockCode(stock.getStockCode());

                if (optionalStock.isPresent()) {
                    Stock updateStock = optionalStock.get();
                    updateStock.setDeviation(Math.round(deviation * 100.0) / 100.0);
                    stockRepository.save(updateStock);
                }

                // 카테고리 가져오기 또는 새로 생성
                Category category = categoryRepository.findByCategoryName(stock.getCategory())
                        .orElseGet(() -> {
                            Category newCategory = new Category();
                            newCategory.setCategoryName(stock.getCategory());
                            newCategory.setDeviation(deviation);
                            return categoryRepository.save(newCategory);
                        });

                // 이미 존재하는 경우 평균 계산 후 업데이트
                if (category.getId() != null) {
                    double newDeviation = (category.getDeviation() + deviation) / 2;
                    category.setDeviation(Math.round(newDeviation * 100.0) / 100.0); // 소수점 두 자리 반올림
                    categoryRepository.save(category);
                }
            }
        } catch (Exception e) {
            log.error("카테고리 업데이트 중 오류 발생: {}", e.getMessage());
        }
    }

    public List<Category> getRecommendedList() {
        List<Category> updatedCategories = categoryRepository.findAll();

        // 편차 기준으로 정렬(내림차순)
        List<Category> sortedCategories = updatedCategories.stream()
                .sorted(Comparator.comparingDouble(Category::getDeviation).reversed())
                .toList();

        int size = sortedCategories.size();
        int limit = Math.min(5, size);

        List<Category> result = new ArrayList<>();
        result.addAll(sortedCategories.subList(0, limit)); // 상위 5개
        result.addAll(sortedCategories.subList(Math.max(size - limit, 0), size)); // 하위 5개

        return result;
    }

    public List<Category> getCategoryList() {
        return categoryRepository.findAll();
    }
}
