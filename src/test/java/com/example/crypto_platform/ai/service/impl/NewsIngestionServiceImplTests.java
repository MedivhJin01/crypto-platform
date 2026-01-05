package com.example.crypto_platform.ai.service.impl;

import com.example.crypto_platform.contract.MarketEvent;
import com.example.crypto_platform.ai.dto.TavilyResponse;
import com.example.crypto_platform.ai.mongodb.document.NewsHistory;
import com.example.crypto_platform.ai.mongodb.repository.NewsHistoryRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NewsIngestionServiceImplTests {

    @Mock
    private NewsHistoryRepository newsHistoryRepository;
    @Mock
    private EmbeddingModel embeddingModel;
    @Mock
    private EmbeddingStore<TextSegment> embeddingStore;

    @InjectMocks
    private NewsIngestionServiceImpl newsIngestionService;

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
    void ingestNews_nullTavilyResponse() {
        newsIngestionService.ingestNews(marketEvent, null);
        verifyNoInteractions(newsHistoryRepository, embeddingModel, embeddingStore);
    }

    @Test
    void ingestNews_nullFields() {
        TavilyResponse tavilyResponse = new TavilyResponse();
        tavilyResponse.setQuery("Q");
        tavilyResponse.setResults(List.of());
        newsIngestionService.ingestNews(marketEvent, tavilyResponse);
        verifyNoInteractions(newsHistoryRepository, embeddingModel, embeddingStore);
    }

    @Test
    void ingestNews_validTavilyResponse() {
        TavilyResponse.Result response1 = new TavilyResponse.Result();
        response1.setUrl("https://example.com/1");
        response1.setTitle("Title 1");
        response1.setContent("Some content 1");

        TavilyResponse.Result response2 = new TavilyResponse.Result();
        response2.setUrl("https://example.com/2");
        response2.setTitle("Title 2");
        response2.setContent("Some content 2");

        TavilyResponse tavilyResponse = new TavilyResponse();
        tavilyResponse.setQuery("Q");
        tavilyResponse.setResults(List.of(response1, response2));

        AtomicInteger seq = new AtomicInteger(0);
        when(newsHistoryRepository.save(any(NewsHistory.class)))
                .thenAnswer(invocation -> {
                    NewsHistory arg = invocation.getArgument(0, NewsHistory.class);
                    arg.setId("news-" + seq.incrementAndGet());
                    return arg;
                });

        when(embeddingModel.embedAll(anyList()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    List<TextSegment> segments = (List<TextSegment>) invocation.getArgument(0);
                    List<Embedding> embeddings = segments.stream()
                            .map(s -> Embedding.from(List.of(0.1f, 0.2f, 0.3f)))
                            .toList();
                    return Response.from(embeddings);
                });

        newsIngestionService.ingestNews(marketEvent, tavilyResponse);
        verify(newsHistoryRepository, times(2)).save(any(NewsHistory.class));
        ArgumentCaptor<NewsHistory> captor = ArgumentCaptor.forClass(NewsHistory.class);
        verify(newsHistoryRepository, atLeastOnce()).save(captor.capture());
        List<NewsHistory> history = captor.getAllValues();

        assertEquals(2, history.size());
        for (NewsHistory nh : history) {
            assertEquals("BTC-USDT", nh.getSymbol());
            assertNotNull(nh.getUrl());
            assertNotNull(nh.getTitle());
            assertNotNull(nh.getContent());
            assertFalse(nh.isDirection());
            assertEquals(2.2, nh.getChange(), 1e-9);

            LocalDate expectedDate = LocalDate.of(2024, 12, 15);
            assertEquals(expectedDate, nh.getDate());
        }

        verify(embeddingModel, times(2)).embedAll(anyList());
        verify(embeddingStore, times(2)).addAll(anyList(), anyList());
    }


}
