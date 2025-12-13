package com.example.crypto_platform.service.impl;

import com.example.crypto_platform.dto.CsParam;
import com.example.crypto_platform.model.Candlestick;
import com.example.crypto_platform.model.Market;
import com.example.crypto_platform.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Validated
public class CsGetServiceImpl implements CsGetService {

    @Autowired
    private MarketDataService marketDataService;
    @Autowired
    private IntervalParseService intervalParseService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private LockService lockService;

    private static final Logger log = LoggerFactory.getLogger(CsGetServiceImpl.class);

    private BigDecimal strip(BigDecimal v) {
        return v.stripTrailingZeros();
    }

    private String buildLockKey(CsParam csParam) {
        return String.format(
                "lock:agg:%d:%d:%d:%d",
                csParam.getMarketId(),
                csParam.getIntervalMs(),
                csParam.getOpenTime(),
                csParam.getCloseTime()
        );
    }

    private List<Candlestick> aggregateCss(CsParam csParam) {
        final long baseIntervalMs = intervalParseService.toMillis("1m");
        final long intervalMs = csParam.getIntervalMs();
        final long startTime = csParam.getOpenTime();
        CsParam baseCsParam = new CsParam(
                csParam.getMarketId(),
                baseIntervalMs,
                csParam.getLimit(),
                csParam.getExchange(),
                csParam.getSymbol(),
                "1m",
                csParam.getOpenTime(),
                csParam.getCloseTime()
        );

        List<Candlestick> css = marketDataService.getMarketData(baseCsParam);
        Map<Long, List<Candlestick>> withBucket = css.stream()
                .collect(Collectors.groupingBy(
                        c -> (c.getOpenTime() - startTime) / intervalMs
                ));
        return withBucket.entrySet().stream()
                .unordered()
                .parallel()
                .map(entry -> {
                    List<Candlestick> bucketCs = entry.getValue();

                    Candlestick firstCs = bucketCs.stream().min(Comparator.comparing(Candlestick::getOpenTime)).orElseThrow();
                    Candlestick lastCs =  bucketCs.stream().max(Comparator.comparing(Candlestick::getOpenTime)).orElseThrow();
                    BigDecimal highPrice = bucketCs.stream().map(Candlestick::getHighPrice).max(Comparator.naturalOrder()).orElseThrow();
                    BigDecimal lowPrice = bucketCs.stream().map(Candlestick::getLowPrice).min(Comparator.naturalOrder()).orElseThrow();
                    BigDecimal volume = bucketCs.stream().map(Candlestick::getVolume).reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new Candlestick(
                            csParam.getMarketId(),
                            csParam.getIntervalMs(),

                            firstCs.getOpenTime(),
                            lastCs.getCloseTime(),
                            strip(firstCs.getOpenPrice()),
                            strip(highPrice),
                            strip(lowPrice),
                            strip(lastCs.getClosePrice()),
                            strip(volume)
                    );
                })
                .toList();
    }

    @Override
    public List<Candlestick> getAggCss(CsParam csParam) {
        List<Candlestick> cachedCs = redisService.getAggCss(csParam);
        if (!cachedCs.isEmpty() && cachedCs.size() == csParam.getLimit()) {
            log.info("Cache hit for {} aggregated candlesticks", cachedCs.size());
            return cachedCs;
        }
        String lockKey = buildLockKey(csParam);
        cachedCs = lockService.executeWithLock(lockKey, 500, 1000, () -> {
            log.info("Aggregate with distributed lock key {}", lockKey);
            List<Candlestick> cachedInside = redisService.getAggCss(csParam);
            if (!cachedInside.isEmpty() && cachedInside.size() == csParam.getLimit()) {
                return cachedInside;
            }
            List<Candlestick> aggCs = aggregateCss(csParam);
            redisService.saveAggCss(csParam, aggCs);
            return aggCs;
        });

        if (cachedCs == null || cachedCs.isEmpty()) {
            try {Thread.sleep(500);} catch (InterruptedException e) {}
            return redisService.getAggCss(csParam);
        }
        return cachedCs;
    }

    @Override
    public Map<String, Candlestick> getLatestCss(String symbol, Long intervalMs) {
        List<Market> markets = marketDataService.getMarketsBySymbol(symbol);
        long openTime = (System.currentTimeMillis() / intervalMs) * intervalMs;
        return markets.stream()
                .map(m -> Map.entry(
                        m.getExchangeName(),
                        redisService.getLatestCs(m.getId(), intervalMs, openTime)
                ))
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }
}
