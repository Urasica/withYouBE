package com.capstone.withyou.service;

import com.capstone.withyou.dao.Stock;
import com.capstone.withyou.dto.StockDTO;
import com.capstone.withyou.repository.StockRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final PythonScriptService pythonScriptService;

    public StockService(StockRepository stockRepository, PythonScriptService pythonScriptService) {
        this.stockRepository = stockRepository;
        this.pythonScriptService = pythonScriptService;
    }

    @Transactional
    // 종목명, 종목코드 업데이트
    public void updateStockData() throws IOException, InterruptedException {
        stockRepository.deleteAll();
        List<Stock> stocks = pythonScriptService.executePythonScript();
        stockRepository.saveAll(stocks);
    }

    // 종목명으로 종목코드 찾기
    public String getStockCode(String stockName){
        Stock stock = stockRepository.findByStockName(stockName);
        return stock.getStockCode();
    }

    // 종목코드로 종목명 찾기
    public String getStockName(String stockCode){
        Stock stock = stockRepository.findByStockCode(stockCode);
        return stock.getStockName();
    }

    // 주식(코드 및 이름) 리스트 조회
    public List<StockDTO> getStocks(){
        List<Stock> stockList = stockRepository.findAll();

        List<StockDTO> stockDTOList = new ArrayList<>();
        for (Stock stock : stockList) {
            StockDTO dto = new StockDTO();
            dto.setStockCode(stock.getStockCode());
            dto.setStockName(stock.getStockName());
            stockDTOList.add(dto);
        }
        return stockDTOList;
    }
}
