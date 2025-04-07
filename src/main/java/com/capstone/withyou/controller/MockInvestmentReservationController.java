package com.capstone.withyou.controller;

import com.capstone.withyou.dao.UserReserveHistory;
import com.capstone.withyou.dto.StockReserveRequestDTO;
import com.capstone.withyou.service.MockInvestmentReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock/reserve")
public class MockInvestmentReservationController {

    private final MockInvestmentReservationService mockInvestmentReservationService;

    public MockInvestmentReservationController(MockInvestmentReservationService mockInvestmentReservationService) {
        this.mockInvestmentReservationService = mockInvestmentReservationService;
    }

    // 주식 매수 예약
    @PostMapping("/buy")
    public ResponseEntity<String> reserveBuyStock(@RequestBody StockReserveRequestDTO request){
        mockInvestmentReservationService.reserveBuyStock(request.getUserId(), request.getStockCode(),
                request.getQuantity(), request.getTargetPrice());
        return ResponseEntity.ok("Reserved stock purchased successfully");
    }

    // 주식 매도 예약
    @PostMapping("/sell")
    public ResponseEntity<String> reserveSellStock(@RequestBody StockReserveRequestDTO request){
        mockInvestmentReservationService.reserveSellStock(request.getUserId(), request.getStockCode(),
                request.getQuantity(), request.getTargetPrice());
        return ResponseEntity.ok("Reserved stock sold successfully");
    }

    // 주식 예약 취소
    @PostMapping("/history/{userId}/remove/{historyId}")
    public ResponseEntity<Void> removeReserveHistory(@PathVariable String userId, @PathVariable Long historyId) {
        mockInvestmentReservationService.removeReservation(userId, historyId);
        return ResponseEntity.ok().build();
    }

    // 주식 에약 내역 조회
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<UserReserveHistory>> getUserReserveHistory(@PathVariable String userId) {
        return ResponseEntity.ok(mockInvestmentReservationService.getReserveHistory(userId));
    }
}
