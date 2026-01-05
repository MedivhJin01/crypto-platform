package com.example.crypto_platform.backend.config;


import com.example.crypto_platform.backend.dto.ExchangeProp;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "exchanges")
public class ExchangeConfig {
    private Map<String, ExchangeProp> exchangeProps = new HashMap<>();

    public ExchangeProp getExchangeProp(String exchange) {
        return exchangeProps.get(exchange.toUpperCase());
    }
}
