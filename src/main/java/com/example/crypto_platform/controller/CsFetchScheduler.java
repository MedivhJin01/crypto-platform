package com.example.crypto_platform.controller;

import com.example.crypto_platform.config.MarketConfig;
import com.example.crypto_platform.dto.CsParam;
import com.example.crypto_platform.dto.CsRequest;
import com.example.crypto_platform.kafka.CsProducer;
import com.example.crypto_platform.service.IntervalParseService;
import com.example.crypto_platform.service.LockService;
import com.example.crypto_platform.service.MarketDataService;
import com.example.crypto_platform.validation.CsRequestValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class CsFetchScheduler {

    private static final Logger log = LoggerFactory.getLogger(CsFetchScheduler.class);

    private final IntervalParseService intervalParseService;
    private final MarketDataService marketDataService;
    private final LockService lockService;
    private final CsRequestValidation csRequestValidation;
    private final CsProducer csProducer;
    private final MarketConfig marketConfig;
    private final Executor fetchExecutor;

    public CsFetchScheduler(IntervalParseService intervalParseService,
                            MarketDataService marketDataService,
                            LockService lockService,
                            CsRequestValidation csRequestValidation,
                            CsProducer csProducer,
                            MarketConfig marketConfig,
                            Executor fetchExecutor) {
        this.intervalParseService = intervalParseService;
        this.marketDataService = marketDataService;
        this.lockService = lockService;
        this.csRequestValidation = csRequestValidation;
        this.csProducer = csProducer;
        this.marketConfig = marketConfig;
        this.fetchExecutor = fetchExecutor;
    }

    private String buildLockKey(CsParam csParam) {
        return String.format(
                "lock:produce:%d:%d:%d:%d",
                csParam.getMarketId(),
                csParam.getIntervalMs(),
                csParam.getOpenTime(),
                csParam.getCloseTime()
        );
    }

    @Scheduled(cron = "0 * * * * *")
    public void postCs() {
        long baseInterval = intervalParseService.toMillis("1m");
        long currentTime = (System.currentTimeMillis() / baseInterval) * baseInterval;

        long backfillTime = intervalParseService.toMillis("1d");

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (String exchange : marketConfig.getExchanges()) {
            for (String symbol : marketConfig.getSymbols()) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        Long marketId = marketDataService.getMarketId(exchange, symbol);
                        Long lastCloseTime = (marketId == null)
                                ? null
                                : marketDataService.getLastCsCloseTime(marketId, baseInterval);
                        long startTime;
                        if (lastCloseTime == null) {
                            startTime = currentTime - backfillTime;
                            log.info("Backfill for {}/{} (marketId={}): startTime={}, endTime={}",
                                    exchange, symbol, marketId, startTime, currentTime - 1L);
                        } else {
                            startTime = lastCloseTime + 1L;
                            log.info("Fetch for {}/{} (marketId={}): startTime={}, endTime={}",
                                    exchange, symbol, marketId, startTime, currentTime - 1L);
                        }

                        long endTime = currentTime - 1L;
                        if (startTime > endTime) {
                            log.info("No new data for {}/{} (marketId={})", exchange, symbol, marketId);
                            return;
                        }

                        CsRequest csRequest = new CsRequest(
                                exchange,
                                symbol,
                                "1m",
                                startTime,
                                endTime
                        );
                        CsParam csParam = csRequestValidation.validateCsRequest(csRequest);
                        String lockKey = buildLockKey(csParam);
                        lockService.executeWithLock(lockKey, 500, 55_000, () -> {
                            log.info("Acquired lock {} for {}/{} window [{} - {}]",
                                    lockKey, exchange, symbol, startTime, endTime);
                            csProducer.produce(csParam);
                            return null;
                        });
                    } catch (Exception e) {
                        log.error("Error scheduling fetch for {}/{}", exchange, symbol, e);
                    }
                }, fetchExecutor);
                futures.add(future);
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

}
