package com.capstone.withyou.repository;

import com.capstone.withyou.dao.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    ExchangeRate findFirstByOrderByIdDesc();
}
