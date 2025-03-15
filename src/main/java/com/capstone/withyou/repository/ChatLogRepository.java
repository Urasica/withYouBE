package com.capstone.withyou.repository;

import com.capstone.withyou.dao.ChatLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {
    @Query("SELECT c FROM ChatLog c WHERE c.userName = :userName ORDER BY c.date DESC, c.id DESC")
    Slice<ChatLog> findChatsByUser(@Param("userName") String userName, Pageable pageable);

    @Query("SELECT c FROM ChatLog c WHERE c.userName = :userName AND c.id < :lastId ORDER BY c.date DESC, c.id DESC")
    Slice<ChatLog> findNextChats(@Param("userName") String userName, @Param("lastId") Long lastId, Pageable pageable);
}
