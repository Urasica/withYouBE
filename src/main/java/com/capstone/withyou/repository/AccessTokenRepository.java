package com.capstone.withyou.repository;

import com.capstone.withyou.dao.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
    AccessToken findTopByOrderByCreatedAtDesc();
}

