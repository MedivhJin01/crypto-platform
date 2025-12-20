package com.example.crypto_platform.ai.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.CreateCollectionOptions;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.mongodb.IndexMapping;
import dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class EmbedConfig {

    @Bean
    public MongoClient mongoClient(@Value("${spring.data.mongodb.uri}") String mongoUri) {
        return MongoClients.create(mongoUri);
    }

    @Bean
    public EmbeddingModel embeddingModel(
            @Value("${ai.news.gemini.api-key}") String apiKey,
            @Value("${ai.news.embedding.model}") String embedModel
    ) {
        return GoogleAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(embedModel)
                .taskType(GoogleAiEmbeddingModel.TaskType.RETRIEVAL_DOCUMENT)
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(
            MongoClient mongoClient,
            @Value("${spring.data.mongodb.database}") String databaseName,
            @Value("${ai.news.embedding.collection}") String collectionName,
            @Value("${ai.news.embedding.index}") String indexName,
            @Value("${ai.news.embedding.dimension}") int dimension,
            @Value("${ai.news.embedding.max-result-ratio}") long maxResultRatio
    ) {
        CreateCollectionOptions createCollectionOptions = new CreateCollectionOptions();
        IndexMapping indexMapping = IndexMapping.builder()
                .dimension(dimension)
                .metadataFieldNames(Set.of("symbol", "date"))
                .build();

        return MongoDbEmbeddingStore.builder()
                .fromClient(mongoClient)
                .databaseName(databaseName)
                .collectionName(collectionName)
                .indexName(indexName)
                .maxResultRatio(maxResultRatio)
                .indexMapping(indexMapping)
                .createIndex(true)
                .build();
    }
}
