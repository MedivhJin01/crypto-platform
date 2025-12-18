package com.example.crypto_platform.backend.service.impl.exchangeServiceImpl;

import com.example.crypto_platform.backend.config.ExchangeConfig;
import com.example.crypto_platform.backend.dto.CsParam;
import com.example.crypto_platform.backend.dto.ExchangeProp;
import com.example.crypto_platform.backend.dto.exchangeResponse.CryptoResponse;
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

@Service("CRYPTO")
public class CryptoExchangeServiceImpl implements ExchangeService {
    private static final Logger log = LoggerFactory.getLogger(CryptoExchangeServiceImpl.class);

    private final ExchangeConfig exchangeConfig;
    private final ExchangeClientService exchangeClientService;

    public CryptoExchangeServiceImpl(ExchangeConfig exchangeConfig, ExchangeClientService exchangeClientService) {
        this.exchangeConfig = exchangeConfig;
        this.exchangeClientService = exchangeClientService;
    }

    @Override
    public Map<String, ?> buildHttpParams(CsParam csParam) {
        String[] parts = csParam.getSymbol().split("-");
        String base = parts[0];
        String quote = parts[1];
        if ("USDT".equalsIgnoreCase(quote) || "USDC".equalsIgnoreCase(quote)) {
            quote = "USD";
        }
        return Map.of(
                "instrument_name", base + quote + "-PERP",
                "timeframe", "1m",
                "count", csParam.getLimit(),
                "start_ts", csParam.getOpenTime(),
                "end_ts", csParam.getCloseTime()
        );
    }

    @Override
    public List<Candlestick> fetchCsData(CsParam csParam, Map<String, ?> params) {
        ExchangeProp exchangeProp = exchangeConfig.getExchangeProp("CRYPTO");
        log.info("[CRYPTO] baseUrl={} path={}", exchangeProp.getBaseUrl(), exchangeProp.getPath());

        CryptoResponse response = exchangeClientService.get(
                exchangeProp.getBaseUrl(),
                exchangeProp.getPath(),
                params,
                new ParameterizedTypeReference<CryptoResponse>() {}
        );
        List<Map<String, Object>> data = response.getResult().getData();
        return data.stream()
                .map(m -> {
                    long t = ((Number) m.get("t")).longValue();
                    return new Candlestick(
                            csParam.getMarketId(),
                            60_000L,
                            t,
                            t + 59_999L,
                            new BigDecimal(m.get("o").toString()),
                            new BigDecimal(m.get("h").toString()),
                            new BigDecimal(m.get("l").toString()),
                            new BigDecimal(m.get("c").toString()),
                            new BigDecimal(m.get("v").toString())
                    );
                }).toList();
    }
}
