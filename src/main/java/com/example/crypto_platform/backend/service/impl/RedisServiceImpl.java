package com.example.crypto_platform.backend.service.impl;

import com.example.crypto_platform.backend.dto.CsParam;
import com.example.crypto_platform.backend.model.Candlestick;
import com.example.crypto_platform.backend.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Candlestick> csRedisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public RedisServiceImpl(@Qualifier("csRedisTemplate") RedisTemplate<String, Candlestick> csRedisTemplate,
                            @Qualifier("stringRedisTemplate") StringRedisTemplate stringRedisTemplate) {
        this.csRedisTemplate = csRedisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private String buildSeriesKey(long marketId, long intervalMs) {
        return String.format("series:%d:%d", marketId, intervalMs);
    }

    private String buildLatestKey(long marketId, Long intervalMs) {
        return String.format("latest:%d:%d", marketId, intervalMs);
    }

    private String buildCooldownKey(long marketId, Long intervalMs) {
        return String.format("cooldown:mkevent:%d:%d", marketId, intervalMs);
    }

    @Override
    public void saveLatestCs(Candlestick candlestick) {
        long marketId = candlestick.getMarketId();
        long intervalMs = candlestick.getKlineInterval();
        String key = buildLatestKey(marketId, intervalMs);
        csRedisTemplate.opsForValue().set(key, candlestick, 2, TimeUnit.MINUTES);
    }

    @Override
    public Candlestick getLatestCs(Long marketId, Long intervalMs) {
        String key = buildLatestKey(marketId, intervalMs);
        return csRedisTemplate.opsForValue().get(key);
    }

    @Override
    public void saveAggCss(CsParam csParam, List<Candlestick> candlesticks) {
        long marketId = csParam.getMarketId();
        long intervalMs = csParam.getIntervalMs();
        String key = buildSeriesKey(marketId, intervalMs);
        Set<ZSetOperations.TypedTuple<Candlestick>> tuples = candlesticks.stream()
                .map(cs -> new DefaultTypedTuple<>( cs, (double) cs.getOpenTime()))
                .collect(Collectors.toSet());
        csRedisTemplate.opsForZSet().add(key, tuples);
    }

    @Override
    public List<Candlestick> getAggCss(CsParam csParam) {
        long marketId = csParam.getMarketId();
        long intervalMs = csParam.getIntervalMs();
        long startTime = csParam.getOpenTime();
        long endTime = csParam.getCloseTime();

        String key = buildSeriesKey(marketId, intervalMs);

        return Objects.requireNonNull(csRedisTemplate.opsForZSet()
                        .rangeByScore(key, startTime, endTime))
                .stream().toList();
    }

    @Override
    public boolean tryAcquireMarketEventCooldown(Long marketId, Long intervalMs, long ttlSeconds) {
        String key = buildCooldownKey(marketId, intervalMs);
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", ttlSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(ok);
    }

}
