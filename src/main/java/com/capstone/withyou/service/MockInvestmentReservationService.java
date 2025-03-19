package com.capstone.withyou.service;

import com.capstone.withyou.dao.TransactionStatus;
import com.capstone.withyou.dao.TransactionType;
import com.capstone.withyou.dao.User;
import com.capstone.withyou.dao.UserReserveHistory;
import com.capstone.withyou.dto.UserReserveHistoryDTO;
import com.capstone.withyou.repository.UserRepository;
import com.capstone.withyou.repository.UserReserveHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MockInvestmentReservationService {

    private final MockInvestmentService mockInvestmentService;
    private final StockService stockService;
    private final UserRepository userRepository;
    private final UserReserveHistoryRepository userReserveHistoryRepository;

    // 주식 매수 예약
    @Transactional
    public void reserveBuyStock(String userId, String stockCode, int quantity, Double targetPrice) {
        createReservation(userId, stockCode, quantity, targetPrice, TransactionType.BUY);
    }

    // 주식 매도 예약
    @Transactional
    public void reserveSellStock(String userId, String stockCode, int quantity, Double targetPrice){
        createReservation(userId, stockCode, quantity, targetPrice, TransactionType.SELL);
    }

    private void createReservation(String userId, String stockCode, int quantity, Double targetPrice, TransactionType type) {
        User user = getUser(userId);
        UserReserveHistory reserveHistory = createReserveHistory(user, stockCode, quantity, targetPrice, type);
        userReserveHistoryRepository.save(reserveHistory);
    }

    // 주식 거래 예약 내역 조회
    public List<UserReserveHistoryDTO> getReserveHistory(String userId) {
        User user = getUser(userId);

        List<UserReserveHistory> histories = userReserveHistoryRepository.findByUser(user);
        return histories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 주식 예약 내역 생성
    private UserReserveHistory createReserveHistory(User user, String stockCode, int quantity, Double targetPrice, TransactionType type) {
        UserReserveHistory history = new UserReserveHistory();
        history.setUser(user);
        history.setStockCode(stockCode);
        history.setStockName(stockService.getStockName(stockCode));
        history.setQuantity(quantity);
        history.setTargetPrice(targetPrice);
        history.setTransactionType(type);
        history.setTransactionStatus(TransactionStatus.WAITING);
        history.setReserveDate(LocalDate.now());
        history.setTradeDate(null);
        return history;
    }

    // Entity -> DTO 변환
    private UserReserveHistoryDTO convertToDTO(UserReserveHistory history) {
        return new UserReserveHistoryDTO(
                history.getStockCode(),
                history.getStockName(),
                history.getQuantity(),
                history.getTargetPrice(),
                history.getTransactionType(),
                history.getTransactionStatus(),
                history.getReserveDate(),
                history.getTradeDate()
        );
    }

    private User getUser(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
    }
}