package com.capstone.withyou.scheduler;

import com.capstone.withyou.dao.ExchangeRate;
import com.capstone.withyou.repository.ExchangeRateRepository;
import com.capstone.withyou.service.PythonScriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class ExchangeRateScheduler {

    private final PythonScriptService pythonScriptService;
    private final ExchangeRateRepository exchangeRateRepository;

    // 10분마다 실행
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void updateExchangeRate() {

        // 거래 가능한 시간(오전 9:00 ~ 오후 7:00)
        LocalDateTime now = LocalDateTime.now();
        LocalTime marketOpen = LocalTime.of(9, 0);
        LocalTime marketClose = LocalTime.of(19, 0);

        if (now.toLocalTime().isBefore(marketOpen) || now.toLocalTime().isAfter(marketClose)) {
            // 거래 시간대가 아니면 실행 안함
            return;
        }

        try {
            exchangeRateRepository.deleteAll();

            double exchangeRate = Math.round(Double.parseDouble(
                    pythonScriptService.fetchExchangeRateFromPython()) * 10000) / 10000.0;

            ExchangeRate newRate = new ExchangeRate();
            newRate.setExchangeRate(exchangeRate);
            exchangeRateRepository.save(newRate);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
