package com.example.crypto_platform.ai.service.impl;

import com.example.crypto_platform.contract.MarketEvent;
import com.example.crypto_platform.ai.mongodb.document.NewsHistory;
import com.example.crypto_platform.ai.mongodb.repository.NewsHistoryRepository;
import com.example.crypto_platform.ai.service.LLMService;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LLMServiceImpl implements LLMService {

    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private ChatModel chatModel;
    @Autowired
    private NewsHistoryRepository newsHistoryRepository;
    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    private String buildQuery(MarketEvent marketEvent) {
        String moveWord = marketEvent.isDirection() ? "rally" : "selloff";
        return "Explain the main catalysts behind a sudden crypto " + moveWord + ": " +
                "macro events (Fed rates, CPI/PPI, jobs data), risk-on/risk-off sentiment, ETF/spot flows, " +
                "liquidations / leverage unwind (short/long squeeze), major exchange incidents (outages, hacks), " +
                "regulatory/legal headlines, large on-chain movements (whale transfers), and major protocol/company news. " +
                "Focus on the immediate trigger and the dominant narrative that explains why the move happened.";
    }

    private List<Content> retrieveFromEmbeds(MarketEvent marketEvent) {
        String text = buildQuery(marketEvent);
        String date = Instant.ofEpochMilli(marketEvent.getStartTime())
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .toString();

        Filter filter = Filter.and(
                new IsEqualTo("symbol", marketEvent.getSymbol()),
                new IsEqualTo("date", date));

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .filter(filter)
                .maxResults(3)
                .minScore(0.85)
                .build();

        return contentRetriever.retrieve(Query.from(text));
    }

    private List<String> getNewsIds(List<Content> contents, String key) {
        return contents.stream()
                .map(c -> c.textSegment().metadata().getString(key))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<NewsHistory> retrieveFromNews(List<Content> contents) {
        List<String> newsIds = getNewsIds(contents, "newsId");
        return newsIds.stream()
                .map(id -> newsHistoryRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    private String buildContext(List<NewsHistory> newsHistories) {
        return newsHistories.stream()
                .map(history -> {
                    String title = history.getTitle();
                    String url = history.getUrl();
                    String content = history.getContent();
                    return  "TITLE: " + title + "\nURL" + url + "\nCONTENT: " + content;
                })
                .collect(Collectors.joining("\n\n"));
    }

    private String buildEmbedContext(List<Content> contents) {
        return contents.stream()
                .map(content -> {
                    String title = content.textSegment().metadata().getString("title");
                    String url = content.textSegment().metadata().getString("url");
                    String text = content.textSegment().text();
                    return "TITLE: " + title + "\nURL: " + url + "\nContent: " + text;
                })
                .collect(Collectors.joining("\n\n"));
    }

    private String buildPrompt(MarketEvent marketEvent, String context) {
        return "MarketEvent:\n" +
                "- symbol: " + marketEvent.getSymbol() + "\n" +
                "- date: " + Instant.ofEpochMilli(marketEvent.getStartTime()).atZone(ZoneOffset.UTC).toLocalDate().toString() + "\n" +
                "- direction: " + (marketEvent.isDirection() ? "up" : "down") + "\n" +
                "- change(%): " + marketEvent.getChange() + "\n" +
                "Articles (ground truth):\n" + context + "\n\n" +
                "Task: Find the most likely catalysts for this move." +
                "Output: 1 sentence explains the most likely reason + 1 sentence citing the source";
    }

    private String summarizeFromEmbeds(MarketEvent marketEvent, List<Content> contents) {
        String context = buildEmbedContext(contents);
        String prompt = buildPrompt(marketEvent, context);
        return generateAnswer(prompt);
    }

    private String summarizeFromHistory(MarketEvent marketEvent, List<NewsHistory> newsHistories) {
        String context = buildContext(newsHistories);
        String prompt = buildPrompt(marketEvent, context);
        return generateAnswer(prompt);
    }

    private String generateAnswer(String prompt) {
        ChatRequest request = ChatRequest.builder()
                .messages(List.of(
                        SystemMessage.from("You are a crypto market analyst, you want to find the root cause for a huge market move. Be concise and evidence-based"),
                        UserMessage.from(prompt)
                ))
                .build();
        ChatResponse response = chatModel.chat(request);
        return response.aiMessage().text();
    }

    @Override
    public String reasoningMarketEvent(MarketEvent marketEvent) {
        List<Content> contents = retrieveFromEmbeds(marketEvent);
        if (contents.isEmpty()) {
            return "No relevant news found for " + marketEvent.getSymbol().split("-")[0] +
                    (marketEvent.isDirection() ? " rally " : " selloff ") +
                    marketEvent.getChange() +
                    " on " + Instant.ofEpochMilli(marketEvent.getStartTime()).atZone(ZoneOffset.UTC).toLocalDate() +
                    '.';
        }
        List<NewsHistory> newsHistories = retrieveFromNews(contents);
        if (newsHistories.isEmpty()) {
            return summarizeFromEmbeds(marketEvent, contents);
        }
        return summarizeFromHistory(marketEvent, newsHistories);
    }
}
