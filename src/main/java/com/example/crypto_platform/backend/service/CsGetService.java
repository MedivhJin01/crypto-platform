package com.example.crypto_platform.backend.service;

import com.example.crypto_platform.backend.dto.CsParam;
import com.example.crypto_platform.backend.model.Candlestick;

import java.util.List;
import java.util.Map;

public interface CsGetService {

    List<Candlestick> getAggCss(CsParam csParam);

    Map<String, Candlestick> getLatestCss(String symbol, Long intervalMs);

}
