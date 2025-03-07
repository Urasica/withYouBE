package com.capstone.withyou.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class StockNameCrawler {

    public String crawlStockName(String stockCode) {
        String url;

        if (stockCode.chars().allMatch(Character::isDigit)) {
            url = "https://m.stock.naver.com/domestic/stock/" + stockCode + "/total";
        } else if (stockCode.chars().allMatch(Character::isLetterOrDigit)) {
            url = "https://m.stock.naver.com/worldstock/stock/" + stockCode + ".O/total";
        } else {
            throw new IllegalArgumentException("유효하지 않은 주식 코드입니다.");
        }

        try {
            Document doc = Jsoup.connect(url)
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .get();

            // 주식 종목명
            Element titleMetaTag = doc.select("meta[property=og:title]").first();

            if (titleMetaTag != null) {
                String stockName = titleMetaTag.attr("content");
                stockName = stockName.replace(" - 네이버페이 증권", "");

                System.out.println(stockName);
                return new String(stockName.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

            } else {
                throw new RuntimeException("종목명 정보를 찾을 수 없습니다.");
            }

        } catch (IOException e) {
            throw new RuntimeException("주식 정보를 가져오는 중 오류가 발생했습니다.", e);
        }
    }
}
