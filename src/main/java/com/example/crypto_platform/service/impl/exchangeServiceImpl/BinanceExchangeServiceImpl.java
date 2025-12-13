package com.example.crypto_platform.service.impl.exchangeServiceImpl;

import com.example.crypto_platform.config.ExchangeConfig;
import com.example.crypto_platform.dto.CsParam;
import com.example.crypto_platform.dto.ExchangeProp;
import com.example.crypto_platform.model.Candlestick;
import com.example.crypto_platform.service.ExchangeService;
import com.example.crypto_platform.service.ExchangeClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service("BINANCE")
public class BinanceExchangeServiceImpl implements ExchangeService {

    private static final Logger log = LoggerFactory.getLogger(BinanceExchangeServiceImpl.class);

    private final ExchangeConfig exchangeConfig;
    private final ExchangeClientService exchangeClientService;

    public BinanceExchangeServiceImpl(ExchangeConfig exchangeConfig, ExchangeClientService exchangeClientService) {
        this.exchangeConfig = exchangeConfig;
        this.exchangeClientService = exchangeClientService;
    }

    @Override
    public Map<String, ?> buildHttpParams(CsParam csParam) {
        return Map.of(
                "symbol", csParam.getSymbol().replaceAll("-", ""),
                "interval", "1m",
                "startTime", csParam.getOpenTime(),
                "endTime", csParam.getCloseTime(),
                "limit", csParam.getLimit()
        );
    }

    @Override
    public List<Candlestick> fetchCsData(CsParam csParam, Map<String, ?> params) {
        ExchangeProp exchangeProp = exchangeConfig.getExchangeProp("BINANCE");
        log.info("[BINANCE] baseUrl={} path={}", exchangeProp.getBaseUrl(), exchangeProp.getPath());

        List<List<String>> response = exchangeClientService.get(
                exchangeProp.getBaseUrl(),
                exchangeProp.getPath(),
                params,
                new ParameterizedTypeReference<List<List<String>>>() {}
        );
        return response.stream()
                .map(a -> new Candlestick(
                        csParam.getMarketId(),
                        60_000L,
                        Long.parseLong(a.get(0)),  // startTime
                        Long.parseLong(a.get(6)),  // endTime
                        new BigDecimal(a.get(1)),  // open
                        new BigDecimal(a.get(2)),  // high
                        new BigDecimal(a.get(3)),  // low
                        new BigDecimal(a.get(4)),  // close
                        new BigDecimal(a.get(5))   // volume
                )).toList();
    }
}
