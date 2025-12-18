package com.example.crypto_platform.ai.service.impl;

import com.example.crypto_platform.ai.dto.MarketEvent;
import com.example.crypto_platform.ai.dto.TavilyResponse;
import com.example.crypto_platform.ai.mongodb.document.NewsHistory;
import com.example.crypto_platform.ai.mongodb.repository.NewsHistoryRepository;
import com.example.crypto_platform.ai.service.NewsIngestionService;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class NewsIngestionServiceImpl implements NewsIngestionService {

    @Autowired
    private NewsHistoryRepository newsHistoryRepository;
    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    private static final int MAX_TOKENS = 256;
    private static final int OVERLAP_TOKENS = 32;

    private Document toDocument(NewsHistory newsHistory) {
        String text = newsHistory.getTitle() + "\n\n" + newsHistory.getContent();
        Metadata metadata = new Metadata();
        metadata.put("newsId",  newsHistory.getId());
        metadata.put("symbol", newsHistory.getSymbol());
        metadata.put("url", newsHistory.getUrl());
        metadata.put("title", newsHistory.getTitle());
        metadata.put("date", newsHistory.getDate().toString());
        metadata.put("direction", Boolean.toString(newsHistory.isDirection()));
        metadata.put("change", Double.toString(newsHistory.getChange()));
        return Document.from(text, metadata);
    }

    private List<TextSegment> splitIntoChunks(Document document) {
        DocumentSplitter splitter = DocumentSplitters.recursive(
                MAX_TOKENS,
                OVERLAP_TOKENS
        );
        return splitter.split(document);
    }

    public void ingestNews(MarketEvent marketEvent, TavilyResponse tavilyResponse) {
        if (tavilyResponse == null || tavilyResponse.getResults().isEmpty()) {
            return;
        }

        LocalDate date = Instant.ofEpochMilli(marketEvent.getStartTime()).atZone(ZoneOffset.UTC).toLocalDate();

        tavilyResponse.getResults().stream()
                .parallel()
                .filter(r -> r.getUrl() != null)
                .filter(r -> r.getTitle() != null || r.getContent() != null)
                .forEach(result -> {
                    NewsHistory newsHistory = newsHistoryRepository.save(
                            NewsHistory.builder()
                                    .symbol(marketEvent.getSymbol())
                                    .url(result.getUrl())
                                    .title(result.getTitle())
                                    .content(result.getContent())
                                    .date(date)
                                    .direction(marketEvent.isDirection())
                                    .change(marketEvent.getChange())
                                    .build()
                    );
                    Document document = toDocument(newsHistory);
                    List<TextSegment> segments = splitIntoChunks(document);
                    List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
                    embeddingStore.addAll(embeddings, segments);
                });
    }
}
