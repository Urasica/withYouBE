package com.capstone.withyou.service;

import com.capstone.withyou.dao.Stock;
import com.capstone.withyou.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PythonScriptService {

    private final StockNameCrawler stockNameCrawler;
    private final StockRepository stockRepository;

    // 주식 리스트 가져오기
    public List<Stock> fetchStockListFromPython() throws IOException, InterruptedException {

        Process process = runPythonScript("stock.py");
        List<Stock> stockList = parseStockList(process);

        checkProcessSuccess(process);
        return stockList;
    }

    // 달러 환율 가져오기
    public String fetchExchangeRateFromPython(){

        try {
            Process process = runPythonScript("exchange_rate.py");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            try (reader) {
                String output = reader.readLine();
                checkProcessSuccess(process);
                return output.trim();
            }
        } catch (IOException | InterruptedException e) {
            return "Error: " + e.getMessage();
        }
    }

    // os 분류
    private String getPythonScriptPath(String scriptName) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "src/main/java/com/capstone/withyou/python/" + scriptName; // Windows
        } else {
            return "/app/python/" + scriptName; // Linux
        }
    }

    // 파이썬 코드 실행
    private Process runPythonScript(String scriptName) throws IOException {
        String scriptPath = getPythonScriptPath(scriptName);
        ProcessBuilder processBuilder = new ProcessBuilder("python", scriptPath);
        return processBuilder.start();
    }

    // 프로세스 성공 체크
    private static void checkProcessSuccess(Process process) throws InterruptedException {
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Python script execution failed with exit code: " + exitCode);
        }
    }

    // 주식 정보 읽고 리스트로 변환
    private List<Stock> parseStockList(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        List<Stock> stockList = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            // 종목코드, 종목명 추출
            String[] parts = line.split(" ", 2);

            if (parts.length == 2) {
                String stockCode = parts[0].trim();
                String stockName = parts[1].trim();

                Stock stock = stockRepository.findByStockCode(stockCode)
                        .orElse(new Stock());
                stock.setStockCode(stockCode);

                if (!parts[0].trim().isEmpty() && Character.isDigit(parts[0].trim().charAt(0))) {
                    stock.setStockName(stockName); // 종목이름(국내)
                } else {
                    if(stock.getStockName()==null) { //DB에 없는 경우에만 크롤링
                        String crawledStockName = stockNameCrawler.crawlStockName(parts[0].trim());
                        stock.setStockName("네이버페이 증권".equals(crawledStockName) ? stockName : crawledStockName); // 종목코드(해외)
                    }
                }
                stockList.add(stock);
            }
        }
        return stockList;
    }
}
