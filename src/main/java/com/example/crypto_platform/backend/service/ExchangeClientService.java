package com.example.crypto_platform.backend.service;

import org.springframework.core.ParameterizedTypeReference;

import java.util.Map;

public interface ExchangeClientService {

    <T> T get(String baseUrl, String path, Map<String, ?> q, ParameterizedTypeReference<T> type);

}
