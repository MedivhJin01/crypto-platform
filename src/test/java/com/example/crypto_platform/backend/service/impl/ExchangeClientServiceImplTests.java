package com.example.crypto_platform.backend.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class ExchangeClientServiceImplTests {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec webClientRequestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?>  webClientRequestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private ExchangeClientServiceImpl exchangeClientServiceImpl;

    @Test
    void get_buildRequestAndReturnResponse() {
        String baseUrl = "https://example.com";
        String path = "/path";

        Map<String, ?> httpParams = Map.of("symbol", "BTC-USDT", "limit", 10);
        String expectedBody = "OK";

        when(webClientBuilder.clone()).thenReturn(webClientBuilder);
        when(webClientBuilder.baseUrl(baseUrl)).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        when(webClient.get()).thenReturn(webClientRequestHeadersUriSpec);
        when(webClientRequestHeadersUriSpec.uri(any(Function.class))).thenReturn(webClientRequestHeadersSpec);
//        when(webClientRequestHeadersSpec.header(eq("User-Agent"), anyString())).thenReturn(webClientRequestHeadersSpec);
        when(webClientRequestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(expectedBody));
        ParameterizedTypeReference<String> typeRef = new ParameterizedTypeReference<>() {};

        String actual = exchangeClientServiceImpl.get(baseUrl, path, httpParams, typeRef);

        assertThat(actual).isEqualTo(expectedBody);

        // verify interaction chain
        verify(webClientBuilder).baseUrl(baseUrl);
        verify(webClientBuilder).build();
        verify(webClient).get();
        verify(webClientRequestHeadersUriSpec).uri(any(Function.class));
        verify(webClientRequestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(webClientBuilder, webClient, webClientRequestHeadersUriSpec,
                webClientRequestHeadersSpec, responseSpec);
    }
}
