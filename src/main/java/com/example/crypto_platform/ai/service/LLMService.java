package com.example.crypto_platform.ai.service;

import com.example.crypto_platform.ai.dto.MarketEvent;

public interface LLMService {
    String reasoningMarketEvent(MarketEvent marketEvent);
}
