package com.example.crypto_platform.backend.kafka;

import com.example.crypto_platform.backend.dto.CsBatch;
import com.example.crypto_platform.backend.model.Candlestick;
import com.example.crypto_platform.backend.service.MarketDataService;
import com.example.crypto_platform.backend.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CsConsumer {

    private static final Logger log = LoggerFactory.getLogger(CsConsumer.class);

    @Autowired
    private MarketDataService marketDataService;

    @Autowired
    private RedisService redisService;

    private String buildKey(Candlestick candlestick) {
        return candlestick.getMarketId() + ":" + candlestick.getKlineInterval();
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

        Map<String, Candlestick> latestByKey = mergedCss.stream()
                .collect(Collectors.toMap(
                        this::buildKey,
                        cs -> cs,
                        (cs1, cs2) -> cs1.getCloseTime() >= cs2.getCloseTime() ? cs1 : cs2
                ));

        latestByKey.values().forEach(redisService::saveLatestCs);

        marketDataService.insert(mergedCss);
        log.info("Consumer inserted Css: {}", mergedCss.size());
    }
}
