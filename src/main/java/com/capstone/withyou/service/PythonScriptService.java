package com.capstone.withyou.service;

import com.capstone.withyou.dao.Stock;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class PythonScriptService {

    private final StockNameCrawler stockNameCrawler;

    public PythonScriptService(StockNameCrawler stockNameCrawler) {
        this.stockNameCrawler = stockNameCrawler;
    }

    public List<Stock> executePythonScript() throws IOException, InterruptedException {

        ProcessBuilder processBuilder = new ProcessBuilder("python", "/app/python/stock.py");
        Process process = processBuilder.start();

        List<Stock> stockList = getStocks(process);

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Python script execution failed with exit code: " + exitCode);
        }
        return stockList;
    }

    private List<Stock> getStocks(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        List<Stock> stockList = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            // 종목코드, 종목명 추출
            String[] parts = line.split(" ", 2);

            if (parts.length == 2) {
                Stock stock = new Stock();
                String stockCode = parts[0].trim();
                String stockName = parts[1].trim();
                stock.setStockCode(stockCode); // 종목코드

                if (!parts[0].trim().isEmpty() && Character.isDigit(parts[0].trim().charAt(0))) {
                    stock.setStockName(stockName); // 종목이름(국내)
                } else {
                    String crawledStockName = stockNameCrawler.crawlStockName(parts[0].trim());
                    stock.setStockName("네이버페이 증권".equals(crawledStockName)? stockName: crawledStockName); // 종목코드(해외)
                }
                stockList.add(stock);
            }
        }
        return stockList;
    }
}
