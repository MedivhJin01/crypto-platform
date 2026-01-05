package com.example.crypto_platform.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.trigger")
public class ThresholdConfig {
    private Threshold defaults = new Threshold();
    private Map<String, Threshold> thresholds = new HashMap<>();

    @Data
    public static class Threshold {
        private BigDecimal priceMove;
        private BigDecimal volumeMove;
    }

    public Threshold thresholdFor(String symbol) {
        return thresholds.getOrDefault(symbol, defaults);
    }
}
