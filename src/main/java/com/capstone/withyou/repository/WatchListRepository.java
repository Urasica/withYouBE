package com.capstone.withyou.repository;

import com.capstone.withyou.dao.User;
import com.capstone.withyou.dao.WatchList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchListRepository extends JpaRepository<WatchList, Long> {
    List<WatchList> findByUser(User user);
    Optional<WatchList> findByUserAndStockCode(User user, String stockCode);
    void deleteByUserAndStockCode(User user, String stockCode);
}
