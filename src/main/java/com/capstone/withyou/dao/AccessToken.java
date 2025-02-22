package com.capstone.withyou.dao;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class AccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String accessToken;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public AccessToken() {}

    public AccessToken(String accessToken, LocalDateTime expiresAt) {
        this.accessToken = accessToken;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
