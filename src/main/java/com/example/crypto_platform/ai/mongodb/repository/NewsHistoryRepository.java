package com.example.crypto_platform.ai.mongodb.repository;

import com.example.crypto_platform.ai.mongodb.document.NewsHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NewsHistoryRepository extends MongoRepository<NewsHistory, String> {

}
