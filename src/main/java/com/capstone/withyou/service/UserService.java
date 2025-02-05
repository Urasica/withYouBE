package com.capstone.withyou.service;

import com.capstone.withyou.dao.User;
import com.capstone.withyou.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 유저 가입 처리
    public void registerUser(String userId, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User();
        user.setUserId(userId);
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    // 사용자 가입 여부 확인
    public boolean verifyUser(String userId) {
        return userRepository.findByUserId(userId).isPresent();
    }

    // 유저 검색
    public Optional<User> getUser(String userId) {
        return userRepository.findByUserId(userId);
    }

    // 비밀번호 확인
    public boolean checkPassword(User user, String password) {
        // 입력된 비밈번호 암호화 하여 DB에 저장된 비밀번호와 비교
        String storedPassword = user.getPassword();

        System.out.println(password + " " + storedPassword);
        return passwordEncoder.matches(password, storedPassword);
    }
}
