package com.capstone.withyou.controller;

import com.capstone.withyou.dto.StockTradeRequestDTO;
import com.capstone.withyou.dto.UserTradeHistoryDTO;
import com.capstone.withyou.service.MockInvestmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock")
public class MockInvestmentController {

    private final MockInvestmentService mockInvestmentService;

    public MockInvestmentController(MockInvestmentService mockInvestmentService) {
        this.mockInvestmentService = mockInvestmentService;
    }

    // 주식 매수
    @PostMapping("/buy")
    public ResponseEntity<String> buyStock(@RequestBody StockTradeRequestDTO request){
        mockInvestmentService.buyStock(request.getUserId(), request.getStockCode(), request.getQuantity());
        return ResponseEntity.ok("Stock purchased successfully");
    }

    // 주식 매도
    @PostMapping("/sell")
    public ResponseEntity<String> sellStock(@RequestBody StockTradeRequestDTO request){
        mockInvestmentService.sellStock(request.getUserId(), request.getStockCode(), request.getQuantity());
        return ResponseEntity.ok("Stock sold successfully");
    }

    // 모의 투자 내역 조회
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<UserTradeHistoryDTO>> getUserInvestmentHistory(@PathVariable String userId){
        return ResponseEntity.ok(mockInvestmentService.getInvestmentHistory(userId));
    }
}
