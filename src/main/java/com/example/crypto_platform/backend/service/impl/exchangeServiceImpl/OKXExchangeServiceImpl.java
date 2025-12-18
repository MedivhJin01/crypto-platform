package com.example.crypto_platform.backend.service.impl.exchangeServiceImpl;

import com.example.crypto_platform.backend.config.ExchangeConfig;
import com.example.crypto_platform.backend.dto.CsParam;
import com.example.crypto_platform.backend.dto.ExchangeProp;
import com.example.crypto_platform.backend.dto.exchangeResponse.OKXResponse;
//import com.example.crypto_platform.backend.enums.Interval;
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

@Service("OKX")
public class OKXExchangeServiceImpl implements ExchangeService {

    private static final Logger log = LoggerFactory.getLogger(OKXExchangeServiceImpl.class);

    private final ExchangeConfig exchangeConfig;
    private final ExchangeClientService exchangeClientService;

    public OKXExchangeServiceImpl(ExchangeConfig exchangeConfig, ExchangeClientService exchangeClientService) {
        this.exchangeConfig = exchangeConfig;
        this.exchangeClientService = exchangeClientService;
    }

    @Override
    public Map<String, ?> buildHttpParams(CsParam csParam) {
        return Map.of(
                "instId", csParam.getSymbol(),
                "bar", "1m",
                "after", csParam.getCloseTime() + 1L,
                "before", csParam.getOpenTime() - 1L,
                "limit", csParam.getLimit()
        );
    }

    @Override
    public List<Candlestick> fetchCsData(CsParam csParam, Map<String, ?> params) {
        ExchangeProp exchangeProp = exchangeConfig.getExchangeProp("OKX");
        log.info("[OKX] baseUrl={} path={}", exchangeProp.getBaseUrl(), exchangeProp.getPath());

        OKXResponse response = exchangeClientService.get(
                exchangeProp.getBaseUrl(),
                exchangeProp.getPath(),
                params,
                new ParameterizedTypeReference<OKXResponse>() {}
        );
        List<List<String>> data = response.getData();
        return data.stream()
                .takeWhile(entry -> Long.parseLong(entry.get(0)) <= csParam.getCloseTime())
                .map(a -> new Candlestick(
                        csParam.getMarketId(),
                        60_000L,
                        Long.parseLong(a.get(0)),
                        Long.parseLong(a.get(0)) + 59_999L,
                        new BigDecimal(a.get(1)),          // open
                        new BigDecimal(a.get(2)),          // high
                        new BigDecimal(a.get(3)),          // low
                        new BigDecimal(a.get(4)),          // close
                        new BigDecimal(a.get(5))           // volume
                )).toList();
    }
}

