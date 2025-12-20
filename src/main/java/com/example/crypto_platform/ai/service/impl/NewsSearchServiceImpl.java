package com.example.crypto_platform.ai.service.impl;

import com.example.crypto_platform.ai.dto.MarketEvent;
import com.example.crypto_platform.ai.dto.TavilyResponse;
import com.example.crypto_platform.ai.service.NewsSearchService;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.WebSearchResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class NewsSearchServiceImpl implements NewsSearchService {

    @Autowired
    private WebSearchEngine webSearchEngine;

    private static final Pattern BRACKET_ELLIPSIS = Pattern.compile("\\[\\s*\\.\\.\\.\\s*\\]");
    private static final Pattern MARKDOWN_HEADERS = Pattern.compile("(?m)^\\s{0,3}#{1,6}\\s+");
    private static final Pattern MULTI_SPACES = Pattern.compile("[ \\t\\f\\v]+");
    private static final Pattern MULTI_NEWLINES = Pattern.compile("\\n{2,}");


    private String buildQuery(String symbol, LocalDate startDate, LocalDate endDate, boolean direction) {
        String base = symbol.split("-")[0];
        String moveWord = direction ? "surge" : "drop";
        String dateRange = String.format("%s .. %s",
                startDate.format(DateTimeFormatter.ISO_DATE),
                endDate.format(DateTimeFormatter.ISO_DATE));

        return String.format(
                "Reasons why %s %s on %s",
                base, moveWord, dateRange
        );
    }

    private String cleanNewsContent(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        String cleanedContent = content;
        cleanedContent = cleanedContent.replace("\r\n", "\n").replace("\r", "\n");
        cleanedContent = BRACKET_ELLIPSIS.matcher(cleanedContent).replaceAll("");
        cleanedContent = MARKDOWN_HEADERS.matcher(cleanedContent).replaceAll("");
        cleanedContent = cleanedContent.replaceAll("(?m)[ \\t]+$", "");
        cleanedContent = cleanedContent.replaceAll("(?m)^\\s+", "");
        cleanedContent = MULTI_NEWLINES.matcher(cleanedContent).replaceAll(" ");
        cleanedContent = MULTI_SPACES.matcher(cleanedContent).replaceAll(" ");
        return cleanedContent.trim();
    }

    private List<TavilyResponse.Result> mapResults(WebSearchResults webSearchResults) {
        if (webSearchResults == null || webSearchResults.results() == null || webSearchResults.results().isEmpty()) {
            return List.of();
        }
        return webSearchResults.results().stream()
                .filter(r -> r.snippet() != null && !r.snippet().isBlank())
                .map(r -> {
                    TavilyResponse.Result result = new TavilyResponse.Result();
                    result.setUrl(URLDecoder.decode(r.url().toString(), StandardCharsets.UTF_8));
                    result.setTitle(r.title());
                    result.setContent(cleanNewsContent(r.snippet()));
                    return result;
                })
                .toList();
    }

    @Override
    public TavilyResponse searchNews(MarketEvent marketEvent) {
        Instant start = Instant.ofEpochMilli(marketEvent.getStartTime());
        Instant end = Instant.ofEpochMilli(marketEvent.getEndTime());

        LocalDate startDate = start.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate endDate = end.atZone(ZoneOffset.UTC).toLocalDate();

        String query = buildQuery(marketEvent.getSymbol(), startDate, endDate, marketEvent.isDirection());

        WebSearchRequest request = WebSearchRequest.builder()
                .searchTerms(query)
                .build();

        WebSearchResults results = webSearchEngine.search(request);
        TavilyResponse tavilyResponse = new TavilyResponse();
        tavilyResponse.setQuery(query);
        tavilyResponse.setResults(mapResults(results));
        return tavilyResponse;
    }

}
