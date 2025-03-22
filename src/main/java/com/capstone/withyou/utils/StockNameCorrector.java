package com.capstone.withyou.utils;

import com.capstone.withyou.dao.Stock;
import com.capstone.withyou.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class StockNameCorrector {
    private final StockRepository stockRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String STOCK_CACHE_KEY = "stock_list";

    @Autowired
    public StockNameCorrector(StockRepository stockRepository, RedisTemplate<String, Object> redisTemplate) {
        this.stockRepository = stockRepository;
        this.redisTemplate = redisTemplate;
    }

    public String correctStockName(String userInput) {
        List<Stock> stockList = getStockListFromCache();

        double maxSimilarity = 0.0;
        String bestMatch = null;

        for (Stock stock : stockList) {
            double similarity = calculateSimilarity(userInput, stock.getStockName());
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                bestMatch = stock.getStockCode();
            }
        }

        return (maxSimilarity > 0.7) ? bestMatch : null;
    }

    private List<Stock> getStockListFromCache() {
        // Redis에서 데이터 가져오기
        Object cachedStockList = redisTemplate.opsForValue().get(STOCK_CACHE_KEY);

        if (cachedStockList instanceof List<?>) {
            return (List<Stock>) cachedStockList;
        }

        // Redis에 데이터가 없으면 DB에서 가져와 저장
        List<Stock> stockList = stockRepository.findAll();
        redisTemplate.opsForValue().set(STOCK_CACHE_KEY, stockList, 1, TimeUnit.HOURS); // 1시간 캐싱

        return stockList;
    }

    private double calculateSimilarity(String str1, String str2) {
        int maxLength = Math.max(str1.length(), str2.length());
        int distance = levenshteinDistance(str1, str2);
        return 1.0 - ((double) distance / maxLength);
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                            dp[i - 1][j - 1] + ((s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1)
                    );
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }
}