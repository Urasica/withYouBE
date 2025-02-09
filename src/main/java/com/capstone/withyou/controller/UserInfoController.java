package com.capstone.withyou.controller;

import com.capstone.withyou.dto.UserInfoDTO;
import com.capstone.withyou.service.UserInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

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
        UserInfoDTO userInfo = userInfoService.getUserInfo(userId);
        return ResponseEntity.ok(userInfo);
    }

    // 보유 금액 업데이트
    @PutMapping("/{userId}/balance")
    public ResponseEntity<Void> updateBalance(@PathVariable String userId, @RequestBody BigDecimal balance) {
        userInfoService.updateUserBalance(userId, balance);
        return ResponseEntity.noContent().build();
    }

    // 보유 주식 초기화
    @PostMapping("/{userId}/reset")
    public ResponseEntity<Void> resetStocks(@PathVariable String userId){
        userInfoService.resetUserStock(userId);
        return ResponseEntity.noContent().build();
    }
}
