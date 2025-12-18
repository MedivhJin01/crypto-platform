package com.example.crypto_platform.backend.service.impl.exchangeServiceImpl;

import com.example.crypto_platform.backend.config.ExchangeConfig;
import com.example.crypto_platform.backend.dto.CsParam;
import com.example.crypto_platform.backend.dto.ExchangeProp;
import com.example.crypto_platform.backend.dto.exchangeResponse.BybitResponse;
import com.example.crypto_platform.backend.model.Candlestick;
import com.example.crypto_platform.backend.service.ExchangeClientService;
import com.example.crypto_platform.backend.service.ExchangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service("BYBIT")
public class BybitExchangeServiceImpl implements ExchangeService {
    private static final Logger log = LoggerFactory.getLogger(BybitExchangeServiceImpl.class);

    private final ExchangeConfig exchangeConfig;
    private final ExchangeClientService exchangeClientService;

    public BybitExchangeServiceImpl(ExchangeConfig exchangeConfig, ExchangeClientService exchangeClientService) {
        this.exchangeConfig = exchangeConfig;
        this.exchangeClientService = exchangeClientService;
    }

    @Override
    public Map<String, ?> buildHttpParams(CsParam csParam) {
        return Map.of(
                "category", "spot",
                "symbol", csParam.getSymbol().replaceAll("-", ""),
                "interval", "1",
                "start", csParam.getOpenTime(),
                "end", csParam.getCloseTime(),
                "limit", csParam.getLimit()
        );
    }

    @Override
    public List<Candlestick> fetchCsData(CsParam csParam, Map<String, ?> params) {
        ExchangeProp exchangeProp = exchangeConfig.getExchangeProp("BYBIT");
        log.info("[BYBIT] baseUrl={} path={}", exchangeProp.getBaseUrl(), exchangeProp.getPath());

        BybitResponse response = exchangeClientService.get(
                exchangeProp.getBaseUrl(),
                exchangeProp.getPath(),
                params,
                new ParameterizedTypeReference<BybitResponse>() {}
        );
        List<List<String>> data = response.getResult().getData();
        return data.stream()
                .map(a -> new Candlestick(
                        csParam.getMarketId(),
                        60_000L,
                        Long.parseLong(a.get(0)),
                        Long.parseLong(a.get(0)) + 59_999L,
                        new BigDecimal(a.get(1)),
                        new BigDecimal(a.get(2)),
                        new BigDecimal(a.get(3)),
                        new BigDecimal(a.get(4)),
                        new BigDecimal(a.get(5))
                )).toList();
    }
}
