package com.example.crypto_platform.ai.controller;

import com.example.crypto_platform.ai.dto.AiNewsResponse;
import com.example.crypto_platform.ai.mongodb.document.NewsHistory;
import com.example.crypto_platform.ai.mongodb.repository.NewsHistoryRepository;
import com.example.crypto_platform.ai.service.LLMService;
import com.example.crypto_platform.contract.MarketEvent;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@Tag(name = "AI", description = "AiController Endpoints")
public class AiController {

    private static final Instant DEMO_INSTANT = Instant.parse("2025-12-15T05:00:00.000Z");

    @Autowired
    private LLMService llmService;
    @Autowired
    private NewsHistoryRepository newsHistoryRepository;

    @PostMapping("/demo")
    public AiNewsResponse demo(@RequestParam String symbol) {

        long startTime = DEMO_INSTANT.toEpochMilli();
        long endTime = startTime + 60_000L; // +1m just for demo

        MarketEvent demoEvent = new MarketEvent(
                symbol,
                startTime,
                endTime,
                true,     // direction up (demo)
                0.002      // +2% (demo)
        );
        LocalDate date = Instant.ofEpochMilli(demoEvent.getStartTime())
                .atZone(ZoneOffset.UTC)
                .toLocalDate();

        List<NewsHistory> rows = newsHistoryRepository.findBySymbolAndDate(symbol, date);

        Map<String, String> linksByTitle = new LinkedHashMap<>();
        for (NewsHistory n : rows) {
            if (n == null) continue;

            String title = n.getTitle() == null ? "" : n.getTitle().trim();
            String url = n.getUrl() == null ? "" : n.getUrl().trim();
            if (title.isBlank() || url.isBlank()) continue;

            String key = title;
            int i = 2;
            while (linksByTitle.containsKey(key)) key = title + " (" + i++ + ")";
            linksByTitle.put(key, url);
        }

        String summary = llmService.reasoningDemo(demoEvent);

        return new AiNewsResponse(
                symbol,
                summary,
                linksByTitle,
                DEMO_INSTANT.toString() // -> 2025-12-15T05:00:00Z
        );

    }
}
