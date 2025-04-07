package com.capstone.withyou.service;

import com.capstone.withyou.dao.TransactionStatus;
import com.capstone.withyou.dao.TransactionType;
import com.capstone.withyou.dao.User;
import com.capstone.withyou.dao.UserReserveHistory;
import com.capstone.withyou.exception.NotFoundException;
import com.capstone.withyou.repository.UserRepository;
import com.capstone.withyou.repository.UserReserveHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    // 예약 취소
    @Transactional
    public void removeReservation(String userId, Long reservationId) {
        User user = getUser(userId);

        UserReserveHistory history = userReserveHistoryRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("해당 예약을 찾을 수 없습니다."));

        if(!history.getUser().equals(user)) {
            throw new IllegalArgumentException("해당 예약을 취소할 권한이 없습니다.");
        }

        if (history.getTransactionStatus() != TransactionStatus.WAITING) {
            throw new IllegalStateException("이미 처리된 예약은 취소할 수 없습니다.");
        }

        userReserveHistoryRepository.delete(history);
    }

    // 주식 거래 예약 내역 조회
    public List<UserReserveHistory> getReserveHistory(String userId) {
        User user = getUser(userId);

        List<UserReserveHistory> histories = userReserveHistoryRepository.findByUser(user);

        List<UserReserveHistory> validHistories = new ArrayList<>();

        for (UserReserveHistory history : histories) {
            if (isValidStockCode(history.getStockCode())) {
                validHistories.add(history);
            } else {
                userReserveHistoryRepository.delete(history);
            }
        }

        return validHistories;
    }

    // 주식 코드 유효한지 확인
    private boolean isValidStockCode(String stockCode) {
        return stockService.getStockName(stockCode) != null;
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

    private User getUser(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다."));
    }
}