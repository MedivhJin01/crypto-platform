package com.example.crypto_platform.ai;

import com.example.crypto_platform.ai.dto.MarketEvent;
import com.example.crypto_platform.ai.dto.TavilyResponse;
import com.example.crypto_platform.ai.mongodb.document.NewsHistory;
import com.example.crypto_platform.ai.mongodb.repository.NewsHistoryRepository;
import com.example.crypto_platform.ai.service.NewsIngestionService;
import com.example.crypto_platform.ai.service.NewsSearchService;
import dev.langchain4j.web.search.WebSearchOrganicResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
public class CryptoPlatformAiTests {

    @Autowired
    private NewsSearchService newsSearchService;
    @Autowired
    private NewsIngestionService newsIngestionService;
    @Autowired
    private NewsHistoryRepository newsHistoryRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${ai.news.embedding.collection}")
    private String embeddingCollection;

    @Test
    void newsIngestionTest() throws Exception {
        ZonedDateTime startTime = ZonedDateTime.of(2025, 12, 15, 0, 0, 0, 0, ZoneId.of("America/Toronto"));
        ZonedDateTime endTime = ZonedDateTime.of(2025, 12, 16, 0, 0, 0, 0, ZoneId.of("America/Toronto"));
        MarketEvent marketEvent = new MarketEvent();
        marketEvent.setSymbol("BTC-USDT");
        marketEvent.setStartTime(startTime.toInstant().toEpochMilli());
        marketEvent.setEndTime(endTime.toInstant().toEpochMilli());
        marketEvent.setDirection(false);
        marketEvent.setChange(2.2);

        TavilyResponse response = newsSearchService.searchNews(marketEvent);
        newsIngestionService.ingestNews(marketEvent, response);
    }



}
