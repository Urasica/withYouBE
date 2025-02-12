package com.capstone.withyou.controller;

import com.capstone.withyou.dao.User;
import com.capstone.withyou.dto.LoginDTO;
import com.capstone.withyou.dto.RegisterDTO;
import com.capstone.withyou.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginAndSignUpController {

    private final UserService userService;

    @Autowired
    public LoginAndSignUpController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody RegisterDTO registerDTO) {
        // 이미 존재 하면 가입 실패 및 에러 메세지 전송
        if(userService.verifyUser(registerDTO.getUserId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("UserId already exists");
        }
        try {
            // 회원가입
            userService.registerUser(registerDTO.getUserId(), registerDTO.getPassword());
            return ResponseEntity.status(HttpStatus.OK).body("User registered successfully");
        } catch (Exception e) {
            // 에러 발생 시 실패 메세지 전송
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("register failed by Error");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO) {
        // 사용자 검증
        if (!userService.verifyUser(loginDTO.getUserId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("UserId does not exist");
        }

        // 사용자 가져오기
        User loginUser = userService.getUser(loginDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 비밀번호 검증
        if (!userService.checkPassword(loginUser, loginDTO.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        LoginDTO response = new LoginDTO();
        response.setUserId(loginUser.getUserId());
        response.setBalance(loginUser.getBalance());
        response.setPassword(null);

        return ResponseEntity.status(HttpStatus.OK).body(response.toString());
    }

}
