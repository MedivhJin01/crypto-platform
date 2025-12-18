package com.example.crypto_platform.backend.service.impl;

import com.example.crypto_platform.backend.dto.CsParam;
import com.example.crypto_platform.backend.model.Candlestick;
import com.example.crypto_platform.backend.service.ExchangeService;
import com.example.crypto_platform.backend.service.MarketDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsFetchServiceImplTests {

    @Mock
    private MarketDataService marketDataService;

    @Mock
    private ExchangeService exchangeService;

    @InjectMocks
    private CsFetchServiceImpl csFetchServiceImpl;

    @BeforeEach
    void setUpExchangeServicesMap() {
        Map<String, ExchangeService> map = new HashMap<>();
        map.put("BINANCE", exchangeService);
        ReflectionTestUtils.setField(csFetchServiceImpl, "exchangeServices", map);
    }

    @Test
    void fetch() {
        long startTime = 0L;
        long endTime   = 59_999L;

        CsParam csParam = new CsParam(
                1L,
                60_000L,
                1,
                "BINANCE",
                "BTC-USDT",
                "1m",
                startTime,
                endTime
        );

        when(exchangeService.buildHttpParams(any(CsParam.class)))
                .thenReturn(Collections.emptyMap());

        List<Candlestick> mockRows = List.of(
                new Candlestick(
                        1L,
                        60_000L,
                        0L,
                        59_999L,
                        new BigDecimal("1.0"),
                        new BigDecimal("2.0"),
                        new BigDecimal("0.5"),
                        new BigDecimal("1.5"),
                        new BigDecimal("10")
                )
        );
        when(exchangeService.fetchCsData(any(CsParam.class), anyMap()))
                .thenReturn(mockRows);

        csFetchServiceImpl.fetch(csParam);

        ArgumentCaptor<CsParam> csParamCaptor = ArgumentCaptor.forClass(CsParam.class);
        ArgumentCaptor<List<Candlestick>> rowsCaptor = ArgumentCaptor.forClass(List.class);

        verify(exchangeService, times(1)).buildHttpParams(csParamCaptor.capture());
        verify(exchangeService, times(1)).fetchCsData(csParamCaptor.capture(), anyMap());
        verify(marketDataService, times(1)).insert(rowsCaptor.capture());

        verifyNoMoreInteractions(exchangeService, marketDataService);
    }

}