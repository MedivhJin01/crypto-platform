package com.example.crypto_platform.ai.kafka;

import com.example.crypto_platform.ai.dto.TavilyResponse;
import com.example.crypto_platform.ai.service.LLMService;
import com.example.crypto_platform.ai.service.NewsIngestionService;
import com.example.crypto_platform.ai.service.NewsSearchService;
import com.example.crypto_platform.contract.MarketEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MkEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(MkEventConsumer.class);

    private final NewsIngestionService newsIngestionService;
    private final NewsSearchService newsSearchService;

    public MkEventConsumer(NewsIngestionService newsIngestionService,
                           NewsSearchService newsSearchService){
        this.newsIngestionService = newsIngestionService;
        this.newsSearchService = newsSearchService;
    }

    @KafkaListener(
        topics = "${app.kafka.market-event-topic:market-event}",
            groupId = "mke-consumer-group",
            containerFactory = "marketEventKafkaListenerContainerFactory"
    )
    public void consume(MarketEvent marketEvent){
        if (marketEvent == null) {
            log.warn("Received null MarketEvent");
            return;
        }

        try {
            log.info(
                    "MarketEvent received: symbol={}, startTime={}, direction={}, change={}",
                    marketEvent.getSymbol(),
                    marketEvent.getStartTime(),
                    marketEvent.isDirection(),
                    marketEvent.getChange()
            );
            TavilyResponse tavilyResponse = newsSearchService.searchNews(marketEvent);
            if (tavilyResponse == null){
                log.warn("No TavilyResponse returned for MarketEvent: symbol={}, startTime={}",
                        marketEvent.getSymbol(), marketEvent.getStartTime());
                return;
            }
            newsIngestionService.ingestNews(marketEvent, tavilyResponse);
            log.info("News ingestion completed: symbol={}, startTime={}",
                    marketEvent.getSymbol(), marketEvent.getStartTime());
        } catch (Exception e){
            log.error("Failed handling MarketEvent: {}", marketEvent, e);
            throw e;
        }

    }
}
