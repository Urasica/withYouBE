package com.capstone.withyou.repository;

import com.capstone.withyou.dao.User;
import com.capstone.withyou.dao.UserStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserStockRepository extends JpaRepository<UserStock, Long> {
    List<UserStock> findByUser(User user);
    void deleteAllByUser(User user);
}
