package com.example.crypto_platform.ai.service.impl;

import com.example.crypto_platform.ai.dto.MarketEvent;
import com.example.crypto_platform.ai.dto.TavilyResponse;
import com.example.crypto_platform.ai.service.NewsSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.WebSearchResults;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NewsSearchServiceImpl implements NewsSearchService {

    private final WebSearchEngine webSearchEngine;
    private final ObjectMapper objectMapper;

    public NewsSearchServiceImpl(WebSearchEngine webSearchEngine, ObjectMapper objectMapper) {
        this.webSearchEngine = webSearchEngine;
        this.objectMapper = objectMapper;
    }

    private String buildQuery(String symbol, LocalDate startDate, LocalDate endDate, boolean direction) {
        String base = symbol.split("-")[0];
        String moveWord = direction ? "surge OR rally OR jump OR rise" : "drop OR plunge OR crash OR selloff";
        String dateRange = String.format("%s .. %s",
                startDate.format(DateTimeFormatter.ISO_DATE),
                endDate.format(DateTimeFormatter.ISO_DATE));

        return String.format(
                "%s %s reason OR catalyst OR \"what happened\" OR liquidation OR ETF OR Fed OR CPI date: %s",
                base, moveWord, dateRange
        );
    }

    private List<TavilyResponse.Result> mapResults(WebSearchResults webSearchResults) {
        if (webSearchResults == null || webSearchResults.results() == null || webSearchResults.results().isEmpty()) {
            return List.of();
        }
        return webSearchResults.results().stream()
                .map(r -> {
                    TavilyResponse.Result result = new TavilyResponse.Result();
                    result.setUrl(r.url().toString());
                    result.setTitle(r.title());
                    result.setContent(r.content());
                    return result;
                })
                .toList();
    }

    @Override
    public TavilyResponse searchNews(MarketEvent marketEvent) {
        Instant start = Instant.ofEpochMilli(marketEvent.getStartTime());
        Instant end   = Instant.ofEpochMilli(marketEvent.getEndTime());

        LocalDate startDate = start.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate endDate   = end.atZone(ZoneOffset.UTC).toLocalDate();

        String query = buildQuery(marketEvent.getSymbol(), startDate, endDate, marketEvent.isDirection());

        WebSearchRequest request = WebSearchRequest.builder()
                .searchTerms(query)
                .build();

        WebSearchResults results = webSearchEngine.search(request);

        TavilyResponse tavilyResponse = new TavilyResponse();
        tavilyResponse.setQuery(query);
        tavilyResponse.setResults(mapResults(results));
        return  tavilyResponse;
//        try {
//            return objectMapper.writeValueAsString(tavilyResponse);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to serialize TavilyResponse", e);
//        }
    }

}
