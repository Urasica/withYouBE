package com.capstone.withyou.scheduler;

import com.capstone.withyou.dao.TransactionStatus;
import com.capstone.withyou.dao.TransactionType;
import com.capstone.withyou.dao.UserReserveHistory;
import com.capstone.withyou.repository.UserReserveHistoryRepository;
import com.capstone.withyou.service.MockInvestmentService;
import com.capstone.withyou.service.StockService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class TradeExecutionScheduler {
    private final StockService stockService;
    private final UserReserveHistoryRepository userReserveHistoryRepository;
    private final MockInvestmentService mockInvestmentService;

    @Scheduled(fixedRate = 60000) //60초 마다 실행
    @Transactional
    public void checkPriceAndTrade(){
        userReserveHistoryRepository.findAll()
                .stream().filter(history -> history.getTransactionStatus()==TransactionStatus.WAITING)
                .forEach(this::processTrade);
    }

    private void processTrade(UserReserveHistory reserveHistory) {
        String stockCode = reserveHistory.getStockCode();
        Double targetPrice = reserveHistory.getTargetPrice();
        Double currentPrice = stockService.getCurrentPrice(stockCode);

        // 설정 금액과 일치하면 주식 매수 후, 상태 값 변경 -> COMPLETED
        if(currentPrice.equals(targetPrice)){
            String userId = reserveHistory.getUser().getUserId();
            int quantity = reserveHistory.getQuantity();
            TransactionType type = reserveHistory.getTransactionType();

            //거래 실행
            if(type==TransactionType.BUY){
                mockInvestmentService.buyStock(userId, stockCode, quantity);
            } else {
                mockInvestmentService.sellStock(userId, stockCode, quantity);
            }
            reserveHistory.setTransactionStatus(TransactionStatus.COMPLETED);
            reserveHistory.setTradeDate(LocalDate.now());
            userReserveHistoryRepository.save(reserveHistory);
        }
    }
}
