package com.capstone.withyou.controller;

import com.capstone.withyou.dto.StockCurPriceDTO;
import com.capstone.withyou.dto.StockPriceDTO;
import com.capstone.withyou.dto.StockPriceDayDTO;
import com.capstone.withyou.service.StockPriceService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@RestController("api/stock")
public class StockPriceController {
    private final StockPriceService stockPriceService;
    private static final Set<String> VALID_PERIODS = Set.of("D", "W", "M");

    @Autowired
    public StockPriceController(StockPriceService stockPriceService) {
        this.stockPriceService = stockPriceService;
    }

    @GetMapping("/prices/{stockCode}")
    @Operation(
            summary = "주식 시세 조회",
            description = "일자별로 주식의 시세를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 순위를 조회함",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = StockPriceDTO.class),
                                    examples = @ExampleObject(value =
                                            """
                                            [
                                             {
                                                "date": "2025-02-14",
                                                "openPrice": 56000,
                                                "highPrice": 57300,
                                                "lowPrice": 56000,
                                                "closePrice": 56000,
                                                "volume": 23979780
                                             },
                                             {
                                                "date": "2025-02-13",
                                                "openPrice": 56100,
                                                "highPrice": 56400,
                                                "lowPrice": 55600,
                                                "closePrice": 55800,
                                                "volume": 22448376
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
                                              "errorMessage": "Invalid input value"
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
                                              "errorMessage": "Failed to retrieve stock price"
                                            }
                                            """
                                    )
                            ))

            }
    )
    public ResponseEntity<List<StockPriceDTO>> getStockPrices(
            @PathVariable String stockCode,
            @Parameter(description = "조회 기간 D:1일단위(최근 30일), W:1주단위(최근 30주), M:1달단위(최근 30개월) | 해외: 조건 동일하지만 100건씩 검색")
            @RequestParam(defaultValue = "D") String period) {

        String normalizedPeriod = period.toUpperCase();
        if (!VALID_PERIODS.contains(normalizedPeriod)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid period: " + period);
        }

        List<StockPriceDTO> prices;

        if (stockCode.chars().allMatch(Character::isDigit)) {
            // 숫자로만 구성된 경우 국내 주식으로 처리
            prices = stockPriceService.getDomesticStockPricesByDay(stockCode, period);
        } else if (stockCode.chars().allMatch(Character::isLetterOrDigit)) {
            // 숫자 또는 알파벳으로 구성된 경우 해외 주식으로 처리
            prices = stockPriceService.getOverseasStockPriceByDay(stockCode, period);
        } else {
            // 숫자와 알파벳 이외의 문자가 포함된 경우
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid stock code");
        }

        return ResponseEntity.ok(prices);
    }

    /*
     분봉 조회
     */
    @GetMapping("/prices-today/{stockCode}")
    public ResponseEntity<List<StockPriceDayDTO>> getStockPricesToday(
            @PathVariable String stockCode,
            @Parameter(description = "조회 범위 - 1 입력: 1분봉, 3 입력: 3분봉 ... ")
            @RequestParam(defaultValue = "1") String time
    ) {
        List<StockPriceDayDTO> prices;

        if(!time.chars().allMatch(Character::isDigit)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "입력된 " + time + "은 숫자가 아닙니다.");
        }

        if (stockCode.chars().allMatch(Character::isDigit)) {
            // 숫자로만 구성된 경우 국내 주식으로 처리
            prices = stockPriceService.getDomesticStockPricesDistribution(stockCode, time);
        } else if (stockCode.chars().allMatch(Character::isLetterOrDigit)) {
            // 숫자 또는 알파벳으로 구성된 경우 해외 주식으로 처리
            prices = stockPriceService.getOverseasStockPricesDistribution(stockCode, time);
        } else {
            // 숫자와 알파벳 이외의 문자가 포함된 경우
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid stock code");
        }

        return ResponseEntity.ok(prices);
    }

    /*
     현재가 조회
     */
    @GetMapping("/current-price")
    public ResponseEntity<StockCurPriceDTO> getCurrentPrice(@RequestParam String stockCode) {
        StockCurPriceDTO stockCurPrice;

        if (stockCode.chars().allMatch(Character::isDigit)) {
            stockCurPrice = stockPriceService.getDomesticStockCurPrice(stockCode);
        } else if (stockCode.chars().allMatch(Character::isLetterOrDigit)) {
            stockCurPrice = stockPriceService.getOverseasStockCurPrice(stockCode);
        } else {
            // 숫자와 알파벳 이외의 문자가 포함된 경우
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid stock code");
        }

        if (stockCurPrice == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid stock code");
        }

        return ResponseEntity.ok(stockCurPrice);
    }
}
