package com.capstone.withyou.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "stock_rank_domestic_rise")
@Getter
@Setter
public class StockRankDomesticRise extends StockRank {
}
