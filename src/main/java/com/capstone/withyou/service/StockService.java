package com.capstone.withyou.service;

import com.capstone.withyou.dao.ExchangeRate;
import com.capstone.withyou.dao.Stock;
import com.capstone.withyou.dto.StockCurPriceDTO;
import com.capstone.withyou.exception.NotFoundException;
import com.capstone.withyou.repository.ExchangeRateRepository;
import com.capstone.withyou.repository.StockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class StockService {

    private final StockRepository stockRepository;
    private final PythonScriptService pythonScriptService;
    private final StockPriceService stockPriceService;
    private final ExchangeRateRepository exchangeRateRepository;

    @Transactional
    // 주식 리스트 업데이트
    public void updateStockData() throws IOException, InterruptedException {
        //새로운 주식리스트, 주식코드 리스트 가져오기
        List<Stock> newStocks = pythonScriptService.fetchStockListFromPython();
        Set<String> newStockCodes = newStocks.stream()
                .map(Stock::getStockCode)
                .collect(Collectors.toSet());

        List<Stock> existingStocks = stockRepository.findAll(); //DB에 저장되어있는 주식

        //새로운 주식리스트에 없다면 상장 페지 주식 -> 삭제 -> 업데이트
        List<Stock> delistedStocks= existingStocks.stream()
                .filter(stock -> !newStockCodes.contains(stock.getStockCode()))
                .toList();
        stockRepository.deleteAll(delistedStocks);
        stockRepository.saveAll(newStocks);
    }

    // 종목명으로 종목코드 찾기
    public String getStockCode(String stockName) {
        Stock stock = stockRepository.findByStockName(stockName);
        return stock != null ? stock.getStockCode() : null;
    }

    // 종목코드로 종목명 찾기
    public String getStockName(String stockCode){
        return stockRepository.findByStockCode(stockCode)
                .map(Stock::getStockName)
                .orElse(null);
    }

    // 주식(코드 및 이름) 리스트 조회
    public List<Stock> getStocks(){
        return stockRepository.findAll();
    }

    // 현재 주가 조회
    public Double getCurrentPrice(String stockCode) {
        StockCurPriceDTO stockCurPrice;
        double currentPrice = 0.0;
        if (stockCode.chars().allMatch(Character::isDigit)) { //국내주식
            stockCurPrice = stockPriceService.getDomesticStockCurPrice(stockCode);
            currentPrice=stockCurPrice.getStockPrice();

        } else if (stockCode.chars().allMatch(Character::isLetterOrDigit)) { //해외주식
            stockCurPrice = stockPriceService.getOverseasStockCurPrice(stockCode);

            //환율 조회
            ExchangeRate exchangeRate = exchangeRateRepository.findFirstByOrderByIdDesc();
            currentPrice = Math.round(stockCurPrice.getStockPrice() * exchangeRate.getExchangeRate());

        } else {
            throw new NotFoundException("해당 주식을 찾을 수 없습니다.");
        }
        return currentPrice;
    }
}
