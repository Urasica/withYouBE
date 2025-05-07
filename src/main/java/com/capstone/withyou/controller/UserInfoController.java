package com.capstone.withyou.controller;

import com.capstone.withyou.dto.UserInfoDTO;
import com.capstone.withyou.dto.UserProfitDTO;
import com.capstone.withyou.service.UserInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user-info")
public class UserInfoController {

    private final UserInfoService userInfoService;

    public UserInfoController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    // 사용자 전체 정보 조회
    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoDTO> getUserInfo(@PathVariable String userId) {
        return ResponseEntity.ok(userInfoService.getUserInfo(userId));
    }

    // 모든 사용자들의 수익률 조회
    @GetMapping("/user-profits")
    public ResponseEntity<List<UserProfitDTO>> getUserProfits() {
        return ResponseEntity.ok(userInfoService.getUserProfits());
    }

    // 보유 금액 업데이트
    @PutMapping("/{userId}/balance")
    public ResponseEntity<Void> updateBalance(@PathVariable String userId, @RequestBody Double balance) {
        userInfoService.updateUserBalance(userId, balance);
        return ResponseEntity.noContent().build();
    }

    // 보유 주식 초기화
    @PostMapping("/{userId}/reset")
    public ResponseEntity<Void> resetStocks(@PathVariable String userId){
        userInfoService.resetUserStock(userId);
        return ResponseEntity.noContent().build();
    }

    // 목표 수익률 설정
    @PutMapping("/{userId}/profit-goal")
    public ResponseEntity<Void> setProfitGoal(@PathVariable String userId, @RequestBody Double goal) {
        userInfoService.setProfitGoal(userId, goal);
        return ResponseEntity.noContent().build();
    }

    // 목표 달성률 조회
    @GetMapping("/{userId}/profit-goal/achievement-rate")
    public ResponseEntity<Double> getProfitAchievementRate(@PathVariable String userId) {
        double rate = userInfoService.getProfitGoalAchievementRate(userId);
        return ResponseEntity.ok(rate);
    }
}
