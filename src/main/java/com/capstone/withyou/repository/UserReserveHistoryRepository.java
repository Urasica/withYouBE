package com.capstone.withyou.repository;

import com.capstone.withyou.dao.User;
import com.capstone.withyou.dao.UserReserveHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserReserveHistoryRepository extends JpaRepository<UserReserveHistory, Long>{
    List<UserReserveHistory> findByUser(User user);
}
