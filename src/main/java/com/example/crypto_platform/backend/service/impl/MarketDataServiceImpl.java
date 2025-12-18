package com.example.crypto_platform.backend.service.impl;

import com.example.crypto_platform.backend.dto.CsParam;
import com.example.crypto_platform.backend.mapper.MarketMapper;
import com.example.crypto_platform.backend.model.Candlestick;
import com.example.crypto_platform.backend.mapper.MarketDataMapper;
import com.example.crypto_platform.backend.model.Market;
import com.example.crypto_platform.backend.service.MarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
public class MarketDataServiceImpl implements MarketDataService {
    @Autowired private MarketDataMapper marketDataMapper;
    @Autowired private MarketMapper marketMapper;

    @Override
    public int insert(List<Candlestick> css) { return marketDataMapper.insertBatch(css); }

    @Override
    public List<Candlestick> getMarketData(CsParam csParam) {
        return marketDataMapper.get(csParam);
    }

    @Override
    public Long getMarketId(String exchange, String symbol) { return marketDataMapper.getMarketId(exchange, symbol); }

    @Override
    public Long getLastCsCloseTime(Long marketId, Long intervalMs) { return marketDataMapper.getLastCsCloseTime(marketId, intervalMs); }

    @Override
    public int deleteAll() { return marketDataMapper.deleteAll(); }

    @Override
    public long countAll() { return marketDataMapper.countAll(); }

    @Override
    public List<Market> getMarketsBySymbol(String symbol) {return marketMapper.getMarketsBySymbol(symbol); }
}
