package com.example.crypto_platform.backend.kafka;

import com.example.crypto_platform.backend.dto.CsBatch;
import com.example.crypto_platform.backend.dto.CsParam;
import com.example.crypto_platform.backend.model.Candlestick;
import com.example.crypto_platform.backend.service.ExchangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

@Component
public class CsProducer {

    private static final Logger log = LoggerFactory.getLogger(CsProducer.class);

    @Autowired
    private Map<String, ExchangeService> exchangeServices;

    @Autowired
    private KafkaTemplate<String, CsBatch> kafkaTemplate;

    @Value("${app.kafka.cs-batch-topic:cs-batch-raw}")
    private String csBatchTopic;

    public void produce(CsParam csParam){
        final ExchangeService exchangeService = exchangeServices.get(csParam.getExchange().toUpperCase());
        final long intervalMs = csParam.getIntervalMs();
        final int PAGE_LIMIT = 300;
        final long CHUNK_MS = PAGE_LIMIT * intervalMs;
        final long startTime = csParam.getOpenTime();
        final long endTime = csParam.getCloseTime();

        LongStream.iterate(startTime, t -> t <= endTime, t -> t + CHUNK_MS)
                .unordered()
                .parallel()
                .forEach(pageOpenTime -> {
                    long pageCloseTime = Math.min(pageOpenTime + CHUNK_MS - 1, endTime);
                    int pageLimit = (int) ((pageCloseTime - pageOpenTime) / intervalMs + 1);
                    CsParam pageParam = new CsParam(
                            csParam.getMarketId(),
                            csParam.getIntervalMs(),
                            pageLimit,
                            csParam.getExchange(),
                            csParam.getSymbol(),
                            csParam.getInterval(),
                            pageOpenTime,
                            pageCloseTime
                    );

                    Map<String, ?> httpParams = exchangeService.buildHttpParams(pageParam);
                    List<Candlestick> rows = exchangeService.fetchCsData(pageParam, httpParams);

                    CsBatch csBatch = new CsBatch(rows);
                    kafkaTemplate.send(csBatchTopic, csBatch)
                            .whenComplete((result, ex) -> {
                                if (ex != null) {
                                    log.error("Failed to send Kafka batch: marketId = {}, exchange = {}, symbol = {}, startTime = {}, endTime = {}",
                                            pageParam.getMarketId(), pageParam.getExchange(), pageParam.getSymbol(), pageOpenTime, pageCloseTime, ex);
                                } else {
                                    log.info("Produced batch: marketId={}, startTime={}, endTime={}, size={}", csParam.getMarketId(), pageOpenTime, pageCloseTime, rows.size());
                                }
                            });
                });
    }

}
