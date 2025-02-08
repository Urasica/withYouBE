package com.capstone.withyou.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "stock_rank_overseas_rise")
@Getter
@Setter
public class StockRankOverseasRise extends StockRank {
}
