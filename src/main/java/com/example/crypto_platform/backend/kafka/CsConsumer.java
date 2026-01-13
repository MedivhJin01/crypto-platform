package com.example.crypto_platform.backend.kafka;

import com.example.crypto_platform.backend.config.ThresholdConfig;
import com.example.crypto_platform.backend.dto.CsBatch;
import com.example.crypto_platform.backend.model.Candlestick;
import com.example.crypto_platform.backend.service.CsAlgoService;
import com.example.crypto_platform.backend.service.MarketDataService;
import com.example.crypto_platform.backend.service.RedisService;
import com.example.crypto_platform.contract.MarketEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CsConsumer {

    private static final Logger log = LoggerFactory.getLogger(CsConsumer.class);
//    private static final BigDecimal PRICE_MOVE_THRESHOLD = new BigDecimal("0.002");
//    private static final BigDecimal VOLUME_MOVE_THRESHOLD = new BigDecimal("10.00");

    private final MarketDataService marketDataService;
    private final RedisService redisService;
    private final ThresholdConfig thresholdConfig;
    private final CsAlgoService csAlgoService;
    private final MkEventProducer mkEventProducer;


    public CsConsumer(MarketDataService marketDataService,
                      RedisService redisService,
                      ThresholdConfig thresholdConfig,
                      CsAlgoService csAlgoService,
                      MkEventProducer mkEventProducer
    ) {
        this.marketDataService = marketDataService;
        this.redisService = redisService;
        this.thresholdConfig = thresholdConfig;
        this.csAlgoService = csAlgoService;
        this.mkEventProducer = mkEventProducer;
    }

    private String buildKey(Candlestick candlestick) {
        return candlestick.getMarketId() + ":" + candlestick.getKlineInterval();
    }

    private MarketEvent triggerMarketEvent(String symbol, Candlestick prevCs, Candlestick tmpCs) {
        BigDecimal prevClosePrice = prevCs.getClosePrice();
        BigDecimal prevVolume = prevCs.getVolume();
        BigDecimal tmpClosePrice = tmpCs.getClosePrice();
        BigDecimal tmpVolume = tmpCs.getVolume();

        if (prevClosePrice.compareTo(BigDecimal.ZERO) == 0 || prevVolume.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        BigDecimal priceMoveTh = thresholdConfig.thresholdFor(symbol).getPriceMove();
        // might need to find other algo for volume since it is different in platforms.
        // vote by majority maybe
        BigDecimal volumeMoveTh = thresholdConfig.thresholdFor(symbol).getVolumeMove();

        BigDecimal priceMovePct = tmpClosePrice.subtract(prevClosePrice).divide(prevClosePrice, 8, RoundingMode.HALF_UP);
        BigDecimal volumeMovePct = tmpVolume.subtract(prevVolume).divide(prevVolume, 8, RoundingMode.HALF_UP);
        BigDecimal absPriceMovePct = priceMovePct.abs();
        BigDecimal absVolumeMovePct = volumeMovePct.abs();

        boolean priceMoveTrigger = absPriceMovePct.compareTo(priceMoveTh) >= 0;
        boolean volumeMoveTrigger = absVolumeMovePct.compareTo(volumeMoveTh) >= 0;
        if (priceMoveTrigger &&  volumeMoveTrigger) {
            boolean direction = priceMovePct.compareTo(BigDecimal.ZERO) >= 0;
            return new MarketEvent(
                    symbol,
                    tmpCs.getOpenTime(),
                    tmpCs.getCloseTime(),
                    direction,
                    absPriceMovePct.doubleValue()
            );
        }
        return null;
    }


    @KafkaListener(
            topics = "${app.kafka.cs-batch-topic:cs-batch-raw}",
            groupId = "cs-consumer-group",
            containerFactory = "kafkaBatchListenerContainerFactory"
    )
    public void consume(List<CsBatch> csBatchList) {
        log.info("Consumer received CsBatchList: {}", csBatchList.size());


        List<Candlestick> mergedCss = csBatchList.stream()
                                .flatMap(b -> b.getCss().stream())
                                .toList();

        Map<String, Candlestick> latestCsByKey = mergedCss.stream()
                .collect(Collectors.toMap(
                        this::buildKey,
                        cs -> cs,
                        (cs1, cs2) -> cs1.getCloseTime() >= cs2.getCloseTime() ? cs1 : cs2
                ));

        // need to figure out the marketId -> symbol issue, flink is a good way to aggregate and sink
        // implement this later
        Map<String, Candlestick> avgLatestCsBySymbol = csAlgoService.avgLatestCsBySymbol(latestCsByKey.values().stream().toList());

        latestCsByKey.values().forEach(redisService::saveLatestCs);
        avgLatestCsBySymbol.forEach((k,v) -> {
            if (v == null) return;

            Candlestick prevAvgCs = redisService.getLatestAvgCs(k);
            if (prevAvgCs != null) {
                String symbol = k.split(":", 2)[0];
                MarketEvent marketEvent = triggerMarketEvent(symbol, prevAvgCs, v);
                if (marketEvent != null) {
                    long ttlSeconds = 3600;
                    boolean allowed = redisService.tryAcquireMarketEventCooldown(k, ttlSeconds);
                    if (allowed) {
                        mkEventProducer.produce(marketEvent);
                    }
                }
            }
            redisService.saveLatestAvgCs(k, v);
        });

        marketDataService.insert(mergedCss);
        log.info("Consumer inserted Css: {}", mergedCss.size());
    }
}
