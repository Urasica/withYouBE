package com.capstone.withyou.dao;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
public class StockRankId implements Serializable {
    private String stockCode;
    private StockPeriod period;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockRankId that = (StockRankId) o;
        return Objects.equals(stockCode, that.stockCode) && period == that.period;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stockCode, period);
    }
}
