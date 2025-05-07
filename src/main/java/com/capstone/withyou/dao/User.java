package com.capstone.withyou.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Setter @Getter
public class User {
    @Id
    private String userId;

    @Column(nullable = false)
    private String password;

    private Double balance;

    private Double profitGoal;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserStock> stocks;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserTradeHistory> purchases;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<WatchList> watchLists;
}
