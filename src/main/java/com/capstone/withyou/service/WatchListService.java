package com.capstone.withyou.service;

import com.capstone.withyou.dao.User;
import com.capstone.withyou.dao.WatchList;
import com.capstone.withyou.repository.UserRepository;
import com.capstone.withyou.repository.WatchListRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WatchListService {

    private final UserRepository userRepository;
    private final WatchListRepository watchListRepository;

    public WatchListService(UserRepository userRepository, WatchListRepository watchListRepository) {
        this.userRepository = userRepository;
        this.watchListRepository = watchListRepository;
    }

    // 주식 관심 등록
    public void addToWatchList(String userId, String stockCode){
        User user = findUserById(userId);

        if (watchListRepository.findByUserAndStockCode(user, stockCode).isPresent()) {
            throw new RuntimeException("Stock already in watch list");
        }

        WatchList watchList = new WatchList();
        watchList.setUser(user);
        watchList.setStockCode(stockCode);
        watchListRepository.save(watchList);
    }

    // 주식 관심 해제
    public void removeFromWatchList(String userId, String stockCode){
        User user = findUserById(userId);
        watchListRepository.deleteByUserAndStockCode(user, stockCode);
    }

    public List<String> getWatchList(String userId) {
        User user = findUserById(userId);

        List<WatchList> watchList = watchListRepository.findByUser(user);
        return watchList.stream()
                .map(WatchList::getStockCode)
                .collect(Collectors.toList());
    }

    // 사용자 조회
    private User findUserById(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
    }
}
