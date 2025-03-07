package com.capstone.withyou.service;

import com.capstone.withyou.dao.User;
import com.capstone.withyou.dao.UserStock;
import com.capstone.withyou.dto.StockCurPriceDTO;
import com.capstone.withyou.dto.UserInfoDTO;
import com.capstone.withyou.dto.UserStockDTO;
import com.capstone.withyou.repository.UserRepository;
import com.capstone.withyou.repository.UserStockRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserInfoService {
    private final UserRepository userRepository;
    private final UserStockRepository userStockRepository;
    private final StockPriceService stockPriceService;

    public UserInfoService(UserRepository userRepository, UserStockRepository userStockRepository, StockPriceService stockPriceService) {
        this.userRepository = userRepository;
        this.userStockRepository = userStockRepository;
        this.stockPriceService = stockPriceService;
    }

    // 유저 정보 불러오기
    public UserInfoDTO getUserInfo(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToUserInfoDTO(user);
    }

    // 유저 보유 금액 수정
    public void updateUserBalance(String userId, Double balance) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setBalance(balance);
        userRepository.save(user);
    }

    // 유저 보유 주식 초기화
    public void resetUserStock(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 모든 주식 정보 삭제
        userStockRepository.deleteAllByUser(user);
        user.setBalance(0.0);
        userRepository.save(user);
    }

    // 유저 보유 주식 총 매입, 총 수익, 총 평가, 수익률 계산
    private void calculateUserStockInfo(UserInfoDTO userInfoDTO) {
        double totalPurchase=0.0;
        double totalProfit=0.0;
        double totalEvaluation=0.0;

        for (UserStockDTO stock : userInfoDTO.getStocks()) {
            totalPurchase+=stock.getAveragePurchasePrice()*stock.getQuantity(); //총 매입 금액
            totalProfit+=stock.getProfitAmount();//총 손익 금액
            totalEvaluation+=stock.getTotalAmount(); //총 평가 금액
        }

        userInfoDTO.setTotalPurchase(totalPurchase);
        userInfoDTO.setTotalProfit(totalProfit);
        userInfoDTO.setTotalEvaluation(totalEvaluation);
        userInfoDTO.setTotalProfitRate(calculateProfitRate(totalPurchase, totalProfit)); //수익률
    }

    // 수익률 계산
    private Double calculateProfitRate(Double base, Double profit) {
        if (base == 0) return 0.0;
        double rate = (profit / base) * 100;
        return Double.parseDouble(String.format("%.1f", rate));
    }

    // 현재 주가 조회
    public Double getCurrentPrice(String stockCode) {
        StockCurPriceDTO stockCurPrice;
        if (stockCode.chars().allMatch(Character::isDigit)) {
            stockCurPrice = stockPriceService.getDomesticStockCurPrice(stockCode);
        } else if (stockCode.chars().allMatch(Character::isLetterOrDigit)) {
            stockCurPrice = stockPriceService.getOverseasStockCurPrice(stockCode);
        } else {
            throw new IllegalArgumentException("Invalid stock code");
        }
        return stockCurPrice.getStockPrice();
    }

    // UserInfo -> UserInfoDTO
    private UserInfoDTO convertToUserInfoDTO(User user) {
        UserInfoDTO dto = new UserInfoDTO();
        dto.setUserId(user.getUserId());
        dto.setBalance(user.getBalance());

        List<UserStockDTO> stockDTOs = convertToUserStockDTOList(user);
        dto.setStocks(stockDTOs);

        calculateUserStockInfo(dto);
        return dto;
    }

    // -> UserInfoDTOList
    private List<UserStockDTO> convertToUserStockDTOList(User user) {
        List<UserStock> stocks = userStockRepository.findByUser(user);
        return stocks.stream()
                .map(this::convertToUserStockDTO)
                .collect(Collectors.toList());
    }

    // UserStock -> UserStockDTO
    private UserStockDTO convertToUserStockDTO(UserStock userStock) {
        UserStockDTO dto = new UserStockDTO();
        dto.setStockCode(userStock.getStockCode());
        dto.setStockName(userStock.getStockName());
        dto.setQuantity(userStock.getQuantity());

        Double averagePurchasePrice = userStock.getAveragePurchasePrice();
        dto.setAveragePurchasePrice(averagePurchasePrice);

        // 실시간 주식 정보 설정(현재 주가, 총 금액, 손익 금액, 손익률)
        Double currentPrice = getCurrentPrice(userStock.getStockCode());
        dto.setCurrentPrice(currentPrice); //현재 주가

        double totalAmount = currentPrice * userStock.getQuantity();
        dto.setTotalAmount(totalAmount); //총 금액

        double profitAmount = totalAmount - (averagePurchasePrice * userStock.getQuantity());
        dto.setProfitAmount(profitAmount); //손익 금액

        dto.setProfitRate(calculateProfitRate(averagePurchasePrice*userStock.getQuantity(), profitAmount)); //손익률

        return dto;
    }
}
