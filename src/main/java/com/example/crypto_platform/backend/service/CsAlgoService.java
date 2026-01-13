package com.example.crypto_platform.backend.service;

import com.example.crypto_platform.backend.model.Candlestick;

import java.util.List;
import java.util.Map;

public interface CsAlgoService {
    Map<String, Candlestick> avgLatestCsBySymbol(List<Candlestick> candlesticks);
}
