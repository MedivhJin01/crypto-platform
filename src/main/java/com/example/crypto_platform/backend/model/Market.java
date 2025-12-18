package com.example.crypto_platform.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Market {
    private Long id;

    private Long exchangeId;
    private Long baseAssetId;
    private Long quoteAssetId;

    private String symbol;
    private String exchangeName;
}
