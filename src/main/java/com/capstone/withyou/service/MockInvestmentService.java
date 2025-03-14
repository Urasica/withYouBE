package com.capstone.withyou.service;

import com.capstone.withyou.dao.TransactionType;
import com.capstone.withyou.dao.User;
import com.capstone.withyou.dao.UserTradeHistory;
import com.capstone.withyou.dao.UserStock;
import com.capstone.withyou.dto.UserTradeHistoryDTO;
import com.capstone.withyou.repository.UserTradeHistoryRepository;
import com.capstone.withyou.repository.UserRepository;
import com.capstone.withyou.repository.UserStockRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MockInvestmentService {

    private final UserRepository userRepository;
    private final UserStockRepository userStockRepository;
    private final UserTradeHistoryRepository userTradeHistoryRepository;
    private final UserInfoService userInfoService;
    private final StockService stockService;

    public MockInvestmentService(UserRepository userRepository, UserStockRepository userStockRepository, UserTradeHistoryRepository userTradeHistoryRepository, UserInfoService userInfoService, StockService stockService) {
        this.userRepository = userRepository;
        this.userStockRepository = userStockRepository;
        this.userTradeHistoryRepository = userTradeHistoryRepository;
        this.userInfoService = userInfoService;
        this.stockService = stockService;
    }

    // 모의 투자(주식 매수)
    @Transactional
    public void buyStock(String userId, String stockCode, int quantity) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        // 주식 현재가 가져오기
        Double currentPrice = getCurrentPrice(stockCode);
        Double totalAmount = currentPrice*quantity;
        updateUserBalance(user, totalAmount, true);

        UserStock userStock = userStockRepository.findByUserAndStockCode(user, stockCode)
                .orElse(null);

        // 새로운 주식 구매할 경우
        if(userStock == null) {
            userStock = new UserStock();
            userStock.setUser(user);
            userStock.setStockCode(stockCode);
            userStock.setStockName(stockService.getStockName(stockCode));
            userStock.setQuantity(quantity);
            userStock.setAveragePurchasePrice(currentPrice);
        } else {
            // 이미 보유한 주식을 구매할 경우
            int newQuantity = userStock.getQuantity() + quantity;
            double newAveragePurchasePrice = ((userStock.getAveragePurchasePrice()
                    * userStock.getQuantity()) + totalAmount)/newQuantity;
            newAveragePurchasePrice = Math.round(newAveragePurchasePrice);

            userStock.setQuantity(newQuantity);
            userStock.setAveragePurchasePrice(newAveragePurchasePrice);
        }

        userStockRepository.save(userStock);

        UserTradeHistory history = createInvestmentHistory(user, stockCode, currentPrice,
                quantity, totalAmount, TransactionType.BUY);
        userTradeHistoryRepository.save(history);
    }

    // 모의 투자(주식 매도)
    @Transactional
    public void sellStock(String userId, String stockCode, int quantity) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        UserStock userStock = userStockRepository.findByUserAndStockCode(user, stockCode)
                .orElseThrow(() -> new RuntimeException("stock not found in user's portfolio"));

        // 판매 수량 확인
        if(userStock.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock quantity");
        }

        // 주식 현재가 가져오기
        Double currentPrice = userInfoService.getCurrentPrice(stockCode);
        Double totalAmount = currentPrice*quantity;

        updateUserBalance(user, totalAmount, false);

        // 주식 보유 수량 업데이트
        int newQuantity = userStock.getQuantity() - quantity;
        if(newQuantity==0){
            userStockRepository.delete(userStock);
        } else {
            userStock.setQuantity(newQuantity);
            userStockRepository.save(userStock);
        }

        UserTradeHistory history = createInvestmentHistory(user, stockCode, currentPrice,
                quantity, totalAmount, TransactionType.SELL);
        userTradeHistoryRepository.save(history);
    }

    // 모의 투자 내역 조회
    public List<UserTradeHistoryDTO> getInvestmentHistory(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        List<UserTradeHistory> histories = userTradeHistoryRepository.findByUser(user);
        return histories.stream()
                .map(this::convertToUserInvestmentHistoryDTO)
                .collect(Collectors.toList());
    }

    // 현재 주가 조회
    private Double getCurrentPrice(String stockCode) {
        return userInfoService.getCurrentPrice(stockCode);
    }

    // 잔액 업데이트
    private void updateUserBalance(User user, Double amount, boolean isBuying) {
        if (isBuying) {
            if (user.getBalance().compareTo(amount) < 0) {
                throw new RuntimeException("Not enough balance");
            }
            user.setBalance(user.getBalance()-amount);
        } else {
            user.setBalance(user.getBalance()+amount);
        }
        userRepository.save(user);
    }

    // 모의투자 내역 생성
    private UserTradeHistory createInvestmentHistory(User user, String stockCode, Double currentPrice,
                                                     int quantity, Double totalAmount, TransactionType type) {
        UserTradeHistory history = new UserTradeHistory();
        history.setUser(user);
        history.setStockCode(stockCode);
        history.setStockName(stockService.getStockName(stockCode));
        history.setPurchaseDate(LocalDate.now());
        history.setPurchasePrice(currentPrice);
        history.setQuantity(quantity);
        history.setTotalAmount(totalAmount);
        history.setTransactionType(type);
        return history;
    }

    // Entity -> DTO 변환
    private UserTradeHistoryDTO convertToUserInvestmentHistoryDTO(UserTradeHistory history) {
        return new UserTradeHistoryDTO(
                history.getStockCode(),
                history.getStockName(),
                history.getPurchaseDate(),
                history.getPurchasePrice(),
                history.getQuantity(),
                history.getTotalAmount(),
                history.getTransactionType()
        );
    }
}