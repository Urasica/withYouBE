package com.capstone.withyou.repository;

import com.capstone.withyou.dao.UserTradeHistory;
import com.capstone.withyou.dao.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTradeHistoryRepository extends JpaRepository<UserTradeHistory, Long> {
    List<UserTradeHistory> findByUser(User user);
}
