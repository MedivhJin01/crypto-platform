package com.example.crypto_platform.ai.service.impl;

import com.example.crypto_platform.ai.dto.MarketEvent;
import com.example.crypto_platform.ai.dto.TavilyResponse;
import dev.langchain4j.web.search.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
public class NewsSeachServiceImplTests {
    @Mock
    private WebSearchEngine webSearchEngine;

    @InjectMocks
    private NewsSearchServiceImpl newsSearchService;

    private MarketEvent marketEvent;

    @BeforeEach
    void setUp() {
        marketEvent = new MarketEvent();
        marketEvent.setSymbol("BTC-USDT");
        marketEvent.setStartTime(1734220800000L);
        marketEvent.setEndTime(1734222240000L);
        marketEvent.setDirection(false);
        marketEvent.setChange(2.2);
    }

    @Test
    void searchNews_test(){
        WebSearchOrganicResult result1 = WebSearchOrganicResult.from(
                "Title 1",
                URI.create("https://example.com/1"),
                "Some snippet 1",
                "Some content 1"
        );
        WebSearchOrganicResult result2 = WebSearchOrganicResult.from(
                "Title 2",
                URI.create("https://example.com/2"),
                "Some snippet 2",
                "Some content 2"
        );
        WebSearchInformationResult info = WebSearchInformationResult.from(0L);
        WebSearchResults results = WebSearchResults.from(info, List.of(result1, result2));

        when(webSearchEngine.search(any(WebSearchRequest.class))).thenReturn(results);
        TavilyResponse response = newsSearchService.searchNews(marketEvent);
        ArgumentCaptor<WebSearchRequest> captor = ArgumentCaptor.forClass(WebSearchRequest.class);
        verify(webSearchEngine, times(1)).search(captor.capture());
        WebSearchRequest request = captor.getValue();
        assertThat(response.getQuery()).isNotBlank();
        assertThat(response.getQuery())
                .contains("Reasons")
                .contains("BTC")
                .contains("selloff");

        assertThat(response.getResults()).hasSize(2);

        assertThat(response.getResults().get(0).getTitle()).isEqualTo("Title 1");
        assertThat(response.getResults().get(0).getUrl()).isEqualTo("https://example.com/1");
        assertThat(response.getResults().get(0).getContent()).isEqualTo("Some snippet 1");

        assertThat(response.getResults().get(1).getTitle()).isEqualTo("Title 2");
        assertThat(response.getResults().get(1).getUrl()).isEqualTo("https://example.com/2");
        assertThat(response.getResults().get(1).getContent()).isEqualTo("Some snippet 2");
    }

    @Test
    void searchNews_emptyResults_test(){
        WebSearchInformationResult info = WebSearchInformationResult.from(0L);
        WebSearchResults results = WebSearchResults.from(info, List.of());

        when(webSearchEngine.search(any(WebSearchRequest.class))).thenReturn(results);
        TavilyResponse response = newsSearchService.searchNews(marketEvent);
        assertThat(response.getResults()).isEmpty();
        verify(webSearchEngine, times(1)).search(any(WebSearchRequest.class));

    }
}
