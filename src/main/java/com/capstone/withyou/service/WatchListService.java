package com.capstone.withyou.service;

import com.capstone.withyou.dao.User;
import com.capstone.withyou.dao.WatchList;
import com.capstone.withyou.repository.UserRepository;
import com.capstone.withyou.repository.WatchListRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional
public class WatchListService {

    private final UserRepository userRepository;
    private final WatchListRepository watchListRepository;
    private final StockService stockService;

    // 주식 관심 등록
    public void addToWatchList(String userId, String stockCode){
        User user = findUserById(userId);

        if (watchListRepository.findByUserAndStockCode(user, stockCode).isPresent()) {
            throw new RuntimeException("Stock already in watch list");
        }

        if (stockService.getStockName(stockCode) == null) {
            throw new RuntimeException("Stock code not found");
        }

        WatchList watchList = new WatchList();
        watchList.setUser(user);
        watchList.setStockCode(stockCode);
        watchList.setStockName(stockService.getStockName(stockCode));

        watchListRepository.save(watchList);
    }

    // 주식 관심 해제
    public void removeFromWatchList(String userId, String stockCode){
        User user = findUserById(userId);
        watchListRepository.deleteByUserAndStockCode(user, stockCode);
    }

    // 관심 목록 조회
    public List<String> getWatchList(String userId) {
        User user = findUserById(userId);

        List<WatchList> watchList = watchListRepository.findByUser(user);
        List<String> validStockCodes = new ArrayList<>();
        List<WatchList> expiredWatchList = new ArrayList<>();

        for (WatchList item : watchList) {
            String stockCode = item.getStockCode();
            if (isValidStockCode(stockCode)) {
                validStockCodes.add(stockCode);
            } else {
                expiredWatchList.add(item);
            }
        }

        // 만료된 항목들 삭제
        if (!expiredWatchList.isEmpty()) {
            watchListRepository.deleteAll(expiredWatchList);
        }

        return validStockCodes;
    }

    private boolean isValidStockCode(String stockCode) {
        return stockService.getStockName(stockCode) != null;
    }


    // 사용자 조회
    private User findUserById(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
    }
}
