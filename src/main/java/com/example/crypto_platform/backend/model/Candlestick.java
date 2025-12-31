package com.example.crypto_platform.backend.model;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Candlestick {
    private Long marketId;
    private Long klineInterval;
    private Long openTime;
    private Long closeTime;

    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal closePrice;

    private BigDecimal volume;


    public static Candlestick empty(Long marketId, Long klineInterval) {
        return new Candlestick(
                marketId,
                klineInterval,
                null,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }
}
