package com.example.crypto_platform.backend.service;

import com.example.crypto_platform.backend.dto.CsParam;
import com.example.crypto_platform.backend.model.Candlestick;

import java.util.List;

public interface RedisService {
    void saveLatestCs(Candlestick candlestick);
    Candlestick getLatestCs(Long marketId, Long intervalMs);
    void saveAggCss(CsParam csParam, List<Candlestick> candlesticks);
    List<Candlestick> getAggCss(CsParam csParam);
    void saveLatestAvgCs(String symbol, Candlestick candlestick);
    Candlestick getLatestAvgCs(String key);
    boolean tryAcquireMarketEventCooldown(String key, long ttlSeconds);
}
