package com.capstone.withyou.controller;

import com.capstone.withyou.dto.StockInfoDTO;
import com.capstone.withyou.service.StockInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class StockInfoController {
    private final StockInfoService stockInfoService;

    @Autowired
    public StockInfoController(StockInfoService stockInfoService) {
        this.stockInfoService = stockInfoService;
    }

    @GetMapping("/stock-info/{stockCode}")
    public ResponseEntity<StockInfoDTO> stockInfo(@PathVariable String stockCode) {

        if (stockCode.chars().allMatch(Character::isDigit)) { // 숫자로만 구성된 경우 국내 주식으로 처리
            return ResponseEntity.ok(stockInfoService.getDomesticStockInfo(stockCode));
        } else if (stockCode.chars().allMatch(Character::isLetterOrDigit)) { // 숫자 또는 알파벳으로 구성된 경우 해외 주식으로 처리
            return ResponseEntity.ok(stockInfoService.getOverseasStockInfo(stockCode));
        } else { // 숫자와 알파벳 이외의 문자가 포함된 경우
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid stock code");
        }
    }
}
