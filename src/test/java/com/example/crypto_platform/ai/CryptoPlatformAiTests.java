package com.example.crypto_platform.ai;

import com.example.crypto_platform.ai.service.LLMService;
import com.example.crypto_platform.ai.service.NewsIngestionService;
import com.example.crypto_platform.ai.service.NewsSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
public class CryptoPlatformAiTests {

    @Autowired
    private NewsSearchService newsSearchService;
    @Autowired
    private NewsIngestionService newsIngestionService;
    @Autowired
    private LLMService llmService;

//    @Test
//    void newsIngestionTest() throws Exception {
//        ZonedDateTime startTime = ZonedDateTime.of(2025, 12, 15, 0, 0, 0, 0, ZoneId.of("America/Toronto"));
//        ZonedDateTime endTime = ZonedDateTime.of(2025, 12, 16, 0, 0, 0, 0, ZoneId.of("America/Toronto"));
//        MarketEvent marketEvent = new MarketEvent();
//        marketEvent.setSymbol("BTC-USDT");
//        marketEvent.setStartTime(startTime.toInstant().toEpochMilli());
//        marketEvent.setEndTime(endTime.toInstant().toEpochMilli());
//        marketEvent.setDirection(false);
//        marketEvent.setChange(2.2);
//
//        TavilyResponse response = newsSearchService.searchNews(marketEvent);
//        newsIngestionService.ingestNews(marketEvent, response);
//    }
//
//    @Test
//    void reasoningMarketEventTest() throws Exception {
//        ZonedDateTime startTime = ZonedDateTime.of(2025, 12, 15, 0, 0, 0, 0, ZoneId.of("America/Toronto"));
//        ZonedDateTime endTime = ZonedDateTime.of(2025, 12, 16, 0, 0, 0, 0, ZoneId.of("America/Toronto"));
//        MarketEvent marketEvent = new MarketEvent();
//        marketEvent.setSymbol("BTC-USDT");
//        marketEvent.setStartTime(startTime.toInstant().toEpochMilli());
//        marketEvent.setEndTime(endTime.toInstant().toEpochMilli());
//        marketEvent.setDirection(false);
//        marketEvent.setChange(2.2);
//        String answer = llmService.reasoningMarketEvent(marketEvent);
//        System.out.println(answer);
//    }


}
