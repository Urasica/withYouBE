package com.capstone.withyou.repository;

import com.capstone.withyou.dao.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> findByStockName(String stockName);
    void deleteByStockName(String stockName);

}
