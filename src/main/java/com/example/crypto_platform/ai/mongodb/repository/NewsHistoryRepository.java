package com.example.crypto_platform.ai.mongodb.repository;

import com.example.crypto_platform.ai.mongodb.document.NewsHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface NewsHistoryRepository extends MongoRepository<NewsHistory, String> {
    List<NewsHistory> findBySymbolAndDate(String symbol, LocalDate date);
}
