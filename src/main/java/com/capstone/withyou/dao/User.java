package com.capstone.withyou.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Setter @Getter
public class User {
    @Id
    private String userId;

    @Column(nullable = false)
    private String password;

    private BigDecimal balance;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserStock> stocks;
}