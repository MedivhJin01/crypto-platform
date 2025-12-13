package com.example.crypto_platform.service;

import com.example.crypto_platform.dto.CsParam;
import com.example.crypto_platform.model.Candlestick;
import com.example.crypto_platform.model.Market;

import java.util.List;

public interface MarketDataService {

    int insert(List<Candlestick> css);

    int deleteAll();

    long countAll();

    List<Candlestick> getMarketData(CsParam csParam);

    Long getMarketId(String exchange, String symbol);

    Long getLastCsCloseTime(Long marketId, Long intervalMs);

    List<Market> getMarketsBySymbol(String symbol);
}
