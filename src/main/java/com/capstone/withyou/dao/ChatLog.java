package com.capstone.withyou.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "chat_log", indexes = {
        @Index(name = "idx_user_date_id", columnList = "userName, date, id")
})
public class ChatLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Setter
    private String message;
    @Setter
    private LocalDate date;
    @Setter
    private String userName;

    public ChatLog(String userName, LocalDate now, String message) {
        this.userName = userName;
        this.message = message;
        this.date = now;
    }
}