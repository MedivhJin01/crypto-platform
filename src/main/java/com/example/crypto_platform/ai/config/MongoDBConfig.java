package com.example.crypto_platform.ai.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoDBConfig {
    @Value("${spring.data.mongodb.uri}")
    private String mongoDBUri;

    @Value("${app.mongodb.database}")
    private String databaseName; // crypto_news

    @Value("${app.mongodb.news-collection}")
    private String newsCollection; // news_content

    @Value("${app.mongodb.embedding-collection:news_embeddings}")
    private String embeddingCollection;

    @Value("${app.mongodb.vector-index:embedding}")
    private String vectorIndexName;

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoDBUri);
    }

//    @Bean
//    public EmbeddingModel embeddingModel() {
//        return
//    }
}
