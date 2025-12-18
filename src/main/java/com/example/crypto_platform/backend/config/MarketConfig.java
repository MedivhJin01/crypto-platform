package com.example.crypto_platform.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class MarketConfig {

    private List<String> exchanges;
    private List<String> symbols;

}
