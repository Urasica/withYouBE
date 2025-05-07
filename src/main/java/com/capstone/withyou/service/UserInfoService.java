package com.capstone.withyou.service;

import com.capstone.withyou.dao.User;
import com.capstone.withyou.dao.UserStock;
import com.capstone.withyou.dto.UserInfoDTO;
import com.capstone.withyou.dto.UserProfitDTO;
import com.capstone.withyou.dto.UserStockDTO;
import com.capstone.withyou.exception.NotFoundException;
import com.capstone.withyou.repository.UserRepository;
import com.capstone.withyou.repository.UserStockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class UserInfoService {

    private final UserRepository userRepository;
    private final UserStockRepository userStockRepository;
    private final StockService stockService;

    // 유저 정보 불러오기
    public UserInfoDTO getUserInfo(String userId) {
        User user = getUser(userId);
        return convertToUserInfoDTO(user);
    }

    // 유저 종목 이름 리스트 조회
    public List<String> getUserStockNames(String userId) {
        User user = getUser(userId);
        return userStockRepository.findByUser(user).stream()
                .map(UserStock::getStockName)
                .collect(Collectors.toList());
    }

    // 유저 보유 금액 수정
    public void updateUserBalance(String userId, Double balance) {
        User user = getUser(userId);
        user.setBalance(balance);
        userRepository.save(user);
    }

    // 유저 보유 주식 초기화
    public void resetUserStock(String userId) {
        User user = getUser(userId);

        // 모든 주식 정보 삭제
        userStockRepository.deleteAllByUser(user);
        user.setBalance(0.0);
        userRepository.save(user);
    }

    // 모든 유저 수익률 리스트 조회
    public List<UserProfitDTO> getUserProfits(){
        List<User> users = userRepository.findAll();
        List<UserProfitDTO> profits = new ArrayList<>();

        for (User user : users) {
            UserProfitDTO dto = new UserProfitDTO();
            dto.setUserId(user.getUserId());;

            Double rate = user.getTotalProfitRate();
            dto.setTotalProfit(rate != null ? rate : 0.0);

            profits.add(dto);
        }

        // 내림차순 정렬
        profits.sort((a, b) -> Double.compare(b.getTotalProfit(), a.getTotalProfit()));
        return profits;
    }

    // 유저 수익률 목표 설정
    public void setProfitGoal(String userId, Double profit) {
        User user = getUser(userId);
        user.setProfitGoal(profit);
        userRepository.save(user);
    }

    // 목표 수익 달성률 조회
    public Double getProfitGoalAchievementRate(String userId) {
        UserInfoDTO userInfo = getUserInfo(userId);
        Double profitGoal = userInfo.getProfitGoal();
        Double currentProfitRate = userInfo.getTotalProfitRate();
        double rate = (currentProfitRate / profitGoal) * 100;
        return Math.round(rate * 10.0) / 10.0;
    }

    private User getUser(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다."));
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

    // 수익률 저장
    private void saveTotalProfitRate(String userId, Double profit) {
        User user = getUser(userId);
        user.setTotalProfitRate(profit);
        userRepository.save(user);
    }

    // 수익률 계산
    private Double calculateProfitRate(Double base, Double profit) {
        if (base == 0) return 0.0;
        double rate = (profit / base) * 100;
        return Double.parseDouble(String.format("%.1f", rate));
    }

    // UserInfo -> UserInfoDTO
    private UserInfoDTO convertToUserInfoDTO(User user) {
        UserInfoDTO dto = new UserInfoDTO();
        dto.setUserId(user.getUserId());
        dto.setBalance(user.getBalance());
        dto.setProfitGoal(user.getProfitGoal());

        List<UserStockDTO> stockDTOs = convertToUserStockDTOList(user);
        dto.setStocks(stockDTOs);

        calculateUserStockInfo(dto);
        saveTotalProfitRate(user.getUserId(), dto.getTotalProfitRate());

        return dto;
    }

    // -> UserInfoDTOList
    private List<UserStockDTO> convertToUserStockDTOList(User user) {
        List<UserStock> stocks = userStockRepository.findByUser(user);
        return stocks.stream()
                .map(this::convertToUserStockDTO)
                .collect(Collectors.toList());
    }

    // UserStock -> UserStockDTO // 삭제된 주식 청산 처리 방법 추가
    private UserStockDTO convertToUserStockDTO(UserStock userStock) {
        UserStockDTO dto = new UserStockDTO();
        dto.setStockCode(userStock.getStockCode());
        dto.setStockName(userStock.getStockName());
        dto.setQuantity(userStock.getQuantity());

        Double averagePurchasePrice = userStock.getAveragePurchasePrice();
        dto.setAveragePurchasePrice(averagePurchasePrice);

        // 실시간 주식 정보 설정(현재 주가, 총 금액, 손익 금액, 손익률)
        Double currentPrice;
        if (stockService.getStockName(userStock.getStockCode()) != null)
            currentPrice = stockService.getCurrentPrice(userStock.getStockCode());
        else
            currentPrice = 0.0;
        dto.setCurrentPrice(currentPrice); //현재 주가

        double totalAmount = currentPrice * userStock.getQuantity();
        dto.setTotalAmount(totalAmount); //총 금액

        double profitAmount = totalAmount - (averagePurchasePrice * userStock.getQuantity());
        dto.setProfitAmount(profitAmount); //손익 금액

        dto.setProfitRate(calculateProfitRate(averagePurchasePrice*userStock.getQuantity(), profitAmount)); //손익률
        return dto;
    }
}
