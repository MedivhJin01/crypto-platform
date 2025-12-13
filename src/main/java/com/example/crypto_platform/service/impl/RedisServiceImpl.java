package com.example.crypto_platform.service.impl;

import com.example.crypto_platform.dto.CsParam;
import com.example.crypto_platform.model.Candlestick;
import com.example.crypto_platform.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RedisServiceImpl implements RedisService {

    @Autowired
    private RedisTemplate<String, Candlestick> redisTemplate;

    private String buildSeriesKey(long marketId, long intervalMs) {
        return String.format("series:%d:%d", marketId, intervalMs);
    }

    private String buildLatestKey(long marketId, Long intervalMs) {
        return String.format("latest:%d:%d", marketId, intervalMs);
    }

    @Override
    public void saveLatestCs(Candlestick candlestick) {
        long marketId = candlestick.getMarketId();
        long intervalMs = candlestick.getKlineInterval();
        String key = buildLatestKey(marketId, intervalMs);
        redisTemplate.opsForValue().set(key, candlestick, 1, TimeUnit.MINUTES);
    }

    @Override
    public Candlestick getLatestCs(Long marketId, Long intervalMs, Long openTime) {
        String key = buildLatestKey(marketId, intervalMs);
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void saveAggCss(CsParam csParam, List<Candlestick> candlesticks) {
        long marketId = csParam.getMarketId();
        long intervalMs = csParam.getIntervalMs();
        String key = buildSeriesKey(marketId, intervalMs);
        Set<ZSetOperations.TypedTuple<Candlestick>> tuples = candlesticks.stream()
                .map(cs -> new DefaultTypedTuple<>( cs, (double) cs.getOpenTime()))
                .collect(Collectors.toSet());
        redisTemplate.opsForZSet().add(key, tuples);
    }

    @Override
    public List<Candlestick> getAggCss(CsParam csParam) {
        long marketId = csParam.getMarketId();
        long intervalMs = csParam.getIntervalMs();
        long startTime = csParam.getOpenTime();
        long endTime = csParam.getCloseTime();

        String key = buildSeriesKey(marketId, intervalMs);

        return Objects.requireNonNull(redisTemplate.opsForZSet()
                        .rangeByScore(key, startTime, endTime))
                .stream().toList();
    }
}
