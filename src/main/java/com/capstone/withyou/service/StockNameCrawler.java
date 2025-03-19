package com.capstone.withyou.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;

@Service
public class StockNameCrawler {

    private final Semaphore semaphore = new Semaphore(10);
    private final long REQUEST_INTERVAL = 200;

    public String crawlStockName(String stockCode) {
        String url = "https://m.stock.naver.com/worldstock/stock/" + stockCode + ".O/total";

        try {
            semaphore.acquire();
            try {
                Document doc = Jsoup.connect(url)
                        .header("Content-Type", "text/html; charset=UTF-8")
                        .get();

                // 주식 종목명
                Element titleMetaTag = doc.select("meta[property=og:title]").first();
                if (titleMetaTag != null) {
                    String stockName = titleMetaTag.attr("content");

                    // 정확한 종목명만 추출
                    if (stockName.contains(" - ")) {
                        stockName = stockName.split(" - ")[0]; // " - " 기준으로 문자열 분리
                    }

                    return new String(stockName.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

                } else {
                    throw new RuntimeException("종목명 정보를 찾을 수 없습니다.");
                }
            } finally {
                Thread.sleep(REQUEST_INTERVAL);
                semaphore.release();
            }
        }
        catch (IOException e) {
            throw new RuntimeException("주식 정보를 가져오는 중 오류가 발생했습니다.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("요청 처리중 인터럽트가 발생했습니다.",e);
        }
    }
}

