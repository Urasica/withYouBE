package com.capstone.withyou.controller;

import com.capstone.withyou.dao.StockPeriod;
import com.capstone.withyou.dao.StockRankDomesticFall;
import com.capstone.withyou.dao.StockRankDomesticRise;
import com.capstone.withyou.dao.StockRankDomesticTrade;
import com.capstone.withyou.repository.StockRankDomesticFallRepository;
import com.capstone.withyou.repository.StockRankDomesticRiseRepository;
import com.capstone.withyou.repository.StockRankDomesticTradeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController("/api/stock")
public class StockRankListController {
    private final StockRankDomesticRiseRepository stockRankDomesticRiseRepository;
    private final StockRankDomesticFallRepository stockRankDomesticFallRepository;
    private final StockRankDomesticTradeRepository stockRankDomesticTradeRepository;

    @Autowired
    public StockRankListController(StockRankDomesticRiseRepository stockRankDomesticRiseRepository,
                                   StockRankDomesticFallRepository stockRankDomesticFallRepository,
                                   StockRankDomesticTradeRepository stockRankDomesticTradeRepository) {
        this.stockRankDomesticRiseRepository = stockRankDomesticRiseRepository;
        this.stockRankDomesticFallRepository = stockRankDomesticFallRepository;
        this.stockRankDomesticTradeRepository = stockRankDomesticTradeRepository;
    }

    @GetMapping("/rise")
    @Operation(
            summary = "주식 상승율 순위 조회",
            description = "일간, 주간, 월간, 년간 기준으로 주식 상승율 순위를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 순위를 조회함",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = StockRankDomesticRise.class),
                                    examples = @ExampleObject(value =
                                            """
                                            [
                                             {
                                              "stockCode": "083660",
                                              "period": "DAILY",
                                              "rank": 1,
                                              "stockName": "주식명",
                                              "currentPrice": 1089,
                                              "changePrice": 251,
                                              "changeRate": 29.95,
                                              "tradeVolume": 9593700,
                                              "highestPrice": 1089,
                                              "lowestPrice": 848
                                             },
                                             {
                                              "...": "..."
                                             }
                                            ]
                                            """
                                    )
                            )),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value =
                                            """
                                            {
                                              "errorCode": "INVALID_PERIOD",
                                              "errorMessage": "Invalid period: INVALID"
                                            }
                                            """
                                    )
                            )),
                    @ApiResponse(responseCode = "500", description = "서버 오류",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value =
                                            """
                                            {
                                              "errorCode": "INTERNAL_SERVER_ERROR",
                                              "errorMessage": "Failed to retrieve stock rankings"
                                            }
                                            """
                                    )
                            ))

            }
    )
    public ResponseEntity<List<StockRankDomesticRise>> getRisingStocks(
            @Parameter(description = "조회 기간 (DAILY, WEEKLY, MONTHLY, YEARLY)", example = "DAILY")
            @RequestParam(defaultValue = "DAILY") String period
    ) {
        StockPeriod stockPeriod;
        try {
            stockPeriod = StockPeriod.valueOf(period.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid period: " + period);
        }

        List<StockRankDomesticRise> stocks;
        try {
            stocks = stockRankDomesticRiseRepository.findByPeriodOrderByRank(stockPeriod);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve stock rankings", e);
        }

        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/fall")
    @Operation(
            summary = "주식 하락율 순위 조회",
            description = "일간, 주간, 월간, 년간 기준으로 주식 하락율 순위를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 순위를 조회함",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = StockRankDomesticRise.class),
                                    examples = @ExampleObject(value =
                                            """
                                            [
                                             {
                                              "stockCode": "299910",
                                              "period": "DAILY",
                                              "rank": 1,
                                              "stockName": "주식명",
                                              "currentPrice": 200,
                                              "changePrice": -83,
                                              "changeRate": -29.33,
                                              "tradeVolume": 2605171,
                                              "highestPrice": 243,
                                              "lowestPrice": 185
                                             },
                                             {
                                              "...": "..."
                                             }
                                            ]
                                            """
                                    )
                            )),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value =
                                            """
                                            {
                                              "errorCode": "INVALID_PERIOD",
                                              "errorMessage": "Invalid period: INVALID"
                                            }
                                            """
                                    )
                            )),
                    @ApiResponse(responseCode = "500", description = "서버 오류",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value =
                                            """
                                            {
                                              "errorCode": "INTERNAL_SERVER_ERROR",
                                              "errorMessage": "Failed to retrieve stock rankings"
                                            }
                                            """
                                    )
                            ))

            }
    )
    public ResponseEntity<List<StockRankDomesticFall>> getFallingStocks(
            @Parameter(description = "조회 기간 (DAILY, WEEKLY, MONTHLY, YEARLY)", example = "DAILY")
            @RequestParam(defaultValue = "DAILY") String period
    ) {
        StockPeriod stockPeriod;
        try {
            stockPeriod = StockPeriod.valueOf(period.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid period: " + period);
        }

        List<StockRankDomesticFall> stocks;
        try {
            stocks = stockRankDomesticFallRepository.findByPeriodOrderByRank(stockPeriod);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve stock rankings", e);
        }

        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/trade-volume")
    @Operation(
            summary = "주식 거래량 순위 조회",
            description = "주식 거래량 순위를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 순위를 조회함",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = StockRankDomesticRise.class),
                                    examples = @ExampleObject(value =
                                            """
                                            [
                                             {
                                              "stockCode": "080220",
                                              "rank": 1,
                                              "stockName": "주식명",
                                              "currentPrice": 16710,
                                              "prevTradeVolume": 2367715,
                                              "listingShares": 34442833,
                                              "avgTradeVolume": 32318865,
                                              "tradeVolume": 32318865,
                                              "changePrice": 2550,
                                              "changeRate": 18.01,
                                              "tradeAmountTurnover": 91.36,
                                              "accumulatedTradeAmount": 525832313530
                                             },
                                             {
                                              "...": "..."
                                             }
                                            ]
                                            """
                                    )
                            )),
                    @ApiResponse(responseCode = "500", description = "서버 오류",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value =
                                            """
                                            {
                                              "errorCode": "INTERNAL_SERVER_ERROR",
                                              "errorMessage": "Failed to retrieve stock rankings"
                                            }
                                            """
                                    )
                            ))

            }
    )
    public ResponseEntity<List<StockRankDomesticTrade>> getTradeVolume() {
        List<StockRankDomesticTrade> stocks;
        try {
            stocks = stockRankDomesticTradeRepository.findAllByOrderByRankAsc();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve stock rankings", e);
        }

        return ResponseEntity.ok(stocks);
    }
}
