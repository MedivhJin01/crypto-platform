package com.example.crypto_platform.backend.service.impl;

import com.example.crypto_platform.backend.service.ExchangeClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Service
public class ExchangeClientServiceImpl implements ExchangeClientService {

    @Autowired
    private WebClient.Builder webClientBuilder;
    @Autowired
    private ClientHttpRequestFactoryBuilder<?> clientHttpRequestFactoryBuilder;

    @Override
    public <T> T get(String baseUrl, String path, Map<String, ?> q, ParameterizedTypeReference<T> type) {
        WebClient webClient = webClientBuilder
                .clone()
                .baseUrl(baseUrl)
                .build();
        try {
            return webClient
                    .get()
                    .uri(b -> {
                        var u = b.path(path);
                        if (q != null) q.forEach(u::queryParam);
                        return u.build();
                    })
//                    .header("User-Agent", "Mozilla/5.0")
                    .retrieve()
                    .bodyToMono(type)
                    .retryWhen(
                            Retry.backoff(3, Duration.ofSeconds(1))
                                    .filter(ex ->
                                            ex instanceof WebClientRequestException ||
                                            ex instanceof WebClientResponseException)
                                    .onRetryExhaustedThrow((spec, signal) -> signal.failure())
                    )
                    .block();
        } catch (WebClientRequestException e) {
            System.err.printf(
                    "[ExchangeClient] Network error calling %s%s: %s%n",
                    baseUrl, path, e.getMessage()
            );
            throw e;
        } catch (WebClientResponseException e) {
            System.err.printf(
                    "[ExchangeClient] HTTP %d calling %s%s: body=%s%n",
                    e.getStatusCode().value(), baseUrl, path, e.getResponseBodyAsString()
            );
            throw e;
        }
    }
}
