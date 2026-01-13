package com.example.crypto_platform.backend.service.impl;

import com.example.crypto_platform.backend.config.MarketConfig;
import com.example.crypto_platform.backend.model.Candlestick;
import com.example.crypto_platform.backend.model.Market;
import com.example.crypto_platform.backend.service.CsAlgoService;
import com.example.crypto_platform.backend.service.MarketDataService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CsAlgoServiceImpl implements CsAlgoService {

    private final MarketConfig marketConfig;
    private final MarketDataService marketDataService;

    public CsAlgoServiceImpl(MarketConfig marketConfig,
                             MarketDataService marketDataService) {
        this.marketConfig = marketConfig;
        this.marketDataService = marketDataService;
    }


    private String buildLatestAvgKey(String symbol, Long intervalMs) {
        return symbol + ":" + intervalMs;
    }

    private Candlestick avgLatestCs(List<Candlestick> candlesticks) {
        if (candlesticks == null || candlesticks.isEmpty()) {
            return null;
        }
        List<Candlestick> css = candlesticks.stream()
                .filter(Objects::nonNull)
                .toList();

        if (css.isEmpty()) {
            return null;
        }

        long intervalMs = css.get(0).getKlineInterval();
        long openTime = css.get(0).getOpenTime();
        long closeTime = css.get(0).getCloseTime();

        BigDecimal avgOpenPrice = css.stream()
                .map(Candlestick::getOpenPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(css.size()), 8, RoundingMode.HALF_UP);

        BigDecimal avgHighPrice = css.stream()
                .map(Candlestick::getHighPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(css.size()), 8, RoundingMode.HALF_UP);

        BigDecimal avgLowPrice = css.stream()
                .map(Candlestick::getLowPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(css.size()), 8, RoundingMode.HALF_UP);

        BigDecimal avgClosePrice = css.stream()
                .map(Candlestick::getClosePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(css.size()), 8, RoundingMode.HALF_UP);

        BigDecimal avgVolume = css.stream()
                .map(Candlestick::getVolume)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(css.size()), 8, RoundingMode.HALF_UP);

        return new Candlestick(
                0L,
                intervalMs,
                openTime,
                closeTime,
                avgOpenPrice,
                avgHighPrice,
                avgLowPrice,
                avgClosePrice,
                avgVolume
        );
    }

    @Override
    public Map<String, Candlestick> avgLatestCsBySymbol(List<Candlestick> candlesticks) {
        if (candlesticks == null || candlesticks.isEmpty()) {
            return Map.of();
        }
        return marketConfig.getSymbols().stream()
                .filter(Objects::nonNull)
                .unordered()
                .parallel()
                .flatMap(symbol -> {
                    Set<Long> marketIds = marketDataService.getMarketsBySymbol(symbol).stream()
                            .map(Market::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    if  (marketIds.isEmpty()) {
                        return Stream.empty();
                    }
                    return candlesticks.stream()
                            .filter(Objects::nonNull)
                            .filter(cs -> marketIds.contains(cs.getMarketId()))
                            .collect(Collectors.groupingBy(Candlestick::getKlineInterval))
                            .entrySet()
                            .stream()
                            .map(e -> {
                                Candlestick avg = avgLatestCs(e.getValue());
                                if (avg == null) return null;
                                String key = buildLatestAvgKey(symbol, e.getKey());
                                return Map.entry(key, avg);
                            })
                            .filter(Objects::nonNull);
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a.getCloseTime() >= b.getCloseTime() ? a : b
                ));
    }
}
