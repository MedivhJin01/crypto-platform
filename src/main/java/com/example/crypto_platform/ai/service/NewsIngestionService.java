package com.example.crypto_platform.ai.service;


import com.example.crypto_platform.contract.MarketEvent;
import com.example.crypto_platform.ai.dto.TavilyResponse;

public interface NewsIngestionService {

    void ingestNews(MarketEvent marketEvent, TavilyResponse tavilyResponse);

}
