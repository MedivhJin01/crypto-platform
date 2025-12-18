package com.example.crypto_platform.ai.config;

import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TavilyConfig {

    @Bean
    public WebSearchEngine webSearchEngine(
            @Value("${ai.news.tavily.api-key}") String apiKey,
            @Value("${ai.news.tavily.search-depth}") String searchDepth,
            @Value("${ai.news.tavily.include-raw-content}") boolean includeRawContent
    ) {
        return TavilyWebSearchEngine.builder()
                .apiKey(apiKey)
                .searchDepth(searchDepth)
                .includeRawContent(includeRawContent)
                .build();
    }
}
