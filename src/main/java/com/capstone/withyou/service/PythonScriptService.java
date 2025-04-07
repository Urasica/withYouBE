package com.capstone.withyou.service;

import com.capstone.withyou.dao.Stock;
import com.capstone.withyou.repository.StockRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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

    // 등락률 데이터 계산
    public Map<String, Double> calculateDeviations() throws IOException, InterruptedException {

        // 파이썬 스크립트 실행
        String scriptPath = getPythonScriptPath("calculate_deviation.py");
        ProcessBuilder processBuilder = new ProcessBuilder("python", scriptPath);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        // 텍스트 결과 파싱
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            Map<String, Double> deviations = new HashMap<>();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split(" ");  // 공백으로 분리

                if (parts.length == 2) {
                    String stockCode = parts[0];
                    String deviationStr = parts[1];

                    try {
                        Double deviation = deviationStr.equalsIgnoreCase("None") ? null : Double.parseDouble(deviationStr);
                        deviations.put(stockCode, deviation);
                        log.info("Parsed: stockCode={}, deviation={}", stockCode, deviation); // 로그 추가
                    } catch (NumberFormatException e) {
                        log.warn("편차 값 파싱 실패: {}", deviationStr);
                    }
                }
            }

            process.waitFor();
            if (process.exitValue() != 0) {
                log.error("파이썬 스크립트 실행 오류");
            }

            return deviations;

        } catch (Exception e) {
            log.error("파이썬 스크립트 결과 처리 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("편차 데이터 처리 오류");
        }
    }

    // 주식 정보 읽고 리스트로 변환
    private List<Stock> parseStockList(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        List<Stock> stockList = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            // 종목코드, 종목명, 카테고리 추출
            String[] parts = line.split("\\|");

            if (parts.length == 3) {
                String stockCode = parts[0].trim();
                String stockName = parts[1].trim();
                String industry = parts[2].trim();

                Stock stock = stockRepository.findByStockCode(stockCode)
                        .orElse(new Stock());
                stock.setStockCode(stockCode);

                if (!parts[0].trim().isEmpty() && Character.isDigit(parts[0].trim().charAt(0))) {
                    stock.setStockName(stockName); // 종목이름(국내)
                    stock.setCategory(null);
                } else {
                    if(stock.getStockName()==null) { //DB에 없는 경우에만 크롤링
                        stock.setCategory(industry);
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
