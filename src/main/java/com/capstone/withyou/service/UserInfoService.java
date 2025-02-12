package com.capstone.withyou.service;

import com.capstone.withyou.dao.User;
import com.capstone.withyou.dao.UserStock;
import com.capstone.withyou.dto.UserInfoDTO;
import com.capstone.withyou.dto.UserStockDTO;
import com.capstone.withyou.repository.UserRepository;
import com.capstone.withyou.repository.UserStockRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/*
 * 추가할 것
 * 1. 유저 보유 주식 실현 손익을 계산하는 로직
 * 2. 실시간 주식 정보 설정(현재 주가, 총 금액, 손익 금액, 수익률)
 */

@Service
public class UserInfoService {
    private final UserRepository userRepository;
    private final UserStockRepository userStockRepository;

    public UserInfoService(UserRepository userRepository, UserStockRepository userStockRepository) {
        this.userRepository = userRepository;
        this.userStockRepository = userStockRepository;
    }

    // 유저 정보 불러오기
    public UserInfoDTO getUserInfo(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToUserInfoDTO(user);
    }

    // 유저 보유 금액 수정
    public void updateUserBalance(String userId, BigDecimal balance) {
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
        user.setBalance(BigDecimal.ZERO);
        userRepository.save(user);
    }

    // 유저 보유 주식 총 매입, 총 수익, 총 평가, 수익률, 실현 손익을 계산
    private void calculateUserStockInfo(UserInfoDTO userInfoDTO) {
        BigDecimal totalPurchase=BigDecimal.ZERO;
        BigDecimal totalProfit=BigDecimal.ZERO;
        BigDecimal totalEvaluation=BigDecimal.ZERO;

        for (UserStockDTO stock : userInfoDTO.getStocks()) {
            totalPurchase= totalPurchase.add(stock.getPurchasePrice()
                    .multiply(BigDecimal.valueOf(stock.getQuantity()))); //총 매입 금액
            totalProfit= totalProfit.add(stock.getProfitAmount()); //총 수익 금액
            totalEvaluation= totalEvaluation.add(stock.getTotalAmount()); //총 평가 금액
        }

        userInfoDTO.setTotalPurchase(totalPurchase);
        userInfoDTO.setTotalProfit(totalProfit);
        userInfoDTO.setTotalEvaluation(totalEvaluation);
        userInfoDTO.setTotalProfitRate(calculateProfitRate(totalPurchase, totalProfit));
        // + 실현 손익 설정 수정 필요
        userInfoDTO.setRealizedProfit(BigDecimal.ZERO);
    }

    private BigDecimal calculateProfitRate(BigDecimal base, BigDecimal profit){
        return base.compareTo(BigDecimal.ZERO)!=0
                ? profit.divide(base, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;
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
        dto.setPurchasePrice(userStock.getPurchasePrice());
        // 실시간 주식 정보 설정(현재 주가, 총 금액, 손익 금액, 수익률)
        return dto;
    }
}
