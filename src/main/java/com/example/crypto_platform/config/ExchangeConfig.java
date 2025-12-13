package com.example.crypto_platform.config;


import com.example.crypto_platform.dto.ExchangeProp;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "exchanges")
public class ExchangeConfig {
    private Map<String, ExchangeProp> exchangeProps = new HashMap<>();

    public Map<String, ExchangeProp> getExchangeProps() {
        return exchangeProps;
    }

    public void setExchangeProps(Map<String, ExchangeProp> exchangeProps) {
        this.exchangeProps = exchangeProps;
    }

    public ExchangeProp getExchangeProp(String exchange) {
        return exchangeProps.get(exchange.toUpperCase());
    }
}
