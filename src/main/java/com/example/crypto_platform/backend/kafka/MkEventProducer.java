package com.example.crypto_platform.backend.kafka;

import com.example.crypto_platform.contract.MarketEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MkEventProducer {
    private static final Logger log = LoggerFactory.getLogger(MkEventProducer.class);

    private final KafkaTemplate<String, MarketEvent> marketEventKafkaTemplate;

    @Value("${app.kafka.market-event-topic:market-event}")
    private String marketEventTopic;

    public  MkEventProducer(@Qualifier("marketEventKafkaTemplate") KafkaTemplate<String, MarketEvent> marketEventKafkaTemplate) {
        this.marketEventKafkaTemplate = marketEventKafkaTemplate;
    }

    public void produce(MarketEvent marketEvent) {
        if (marketEvent == null) {
            return;
        }
        marketEventKafkaTemplate.send(marketEventTopic, marketEvent)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error(
                                "Failed to send MarketEvent: symbol={}, start={}, direction={}, change={}",
                                marketEvent.getSymbol(),
                                marketEvent.getStartTime(),
                                marketEvent.isDirection(),
                                marketEvent.getChange(),
                                ex
                        );
                    } else {
                        log.info(
                                "MarketEvent sent: symbol={}, start={}, direction={}, change={}",
                                marketEvent.getSymbol(),
                                marketEvent.getStartTime(),
                                marketEvent.isDirection(),
                                marketEvent.getChange()
                        );
                    }
        });
    }
}
