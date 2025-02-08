package com.capstone.withyou.dao;

import lombok.Getter;

@Getter
public enum StockPeriod {
    DAILY(0),   // 1일
    WEEKLY(5),  // 1주 (영업일 기준 5일)
    MONTHLY(22), // 1달 (대략 한 달 영업일)
    YEARLY(252); // 1년 (연간 영업일 대략 252일)

    private final int days;

    StockPeriod(int days) {
        this.days = days;
    }
}

