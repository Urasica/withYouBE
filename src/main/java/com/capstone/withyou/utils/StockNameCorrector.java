package com.capstone.withyou.utils;

import com.capstone.withyou.dao.Stock;
import com.capstone.withyou.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;;

@Component
public class StockNameCorrector {
    private final StockRepository stockRepository; // 종목명 & 코드 저장된 DB 접근

    @Autowired
    public StockNameCorrector(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public String correctStockName(String userInput) {
        // DB에서 모든 종목명 가져오기 (최대 1만 개)
        List<Stock> stockList = stockRepository.findAll();

        double maxSimilarity = 0.0;
        String bestMatch = null;

        for (Stock stock : stockList) {
            double similarity = calculateSimilarity(userInput, stock.getStockName());
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                bestMatch = stock.getStockCode(); // 종목 코드 반환
            }
        }

        // 유사도가 일정 기준 이상이면 반환, 아니면 null
        return (maxSimilarity > 0.7) ? bestMatch : null;
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
