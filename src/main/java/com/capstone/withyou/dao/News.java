package com.capstone.withyou.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime LastViewTime;

    @Column(nullable = false)
    private String stockName;

    private String title;
    private String link;
    private String summary;
    private String press;
    private String date;
    private String imageUrl;
}
