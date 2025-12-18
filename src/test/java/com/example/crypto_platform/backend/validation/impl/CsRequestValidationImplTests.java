package com.example.crypto_platform.backend.validation.impl;

import com.example.crypto_platform.backend.dto.CsParam;
import com.example.crypto_platform.backend.dto.CsRequest;
import com.example.crypto_platform.backend.exception.CsRequestException;
import com.example.crypto_platform.backend.service.ExchangeService;
import com.example.crypto_platform.backend.service.IntervalParseService;
import com.example.crypto_platform.backend.service.MarketDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CsRequestValidationImplTests {
    @Mock
    private MarketDataService marketDataService;

    @Mock
    private Map<String, ExchangeService> exchangeServices;

    @Mock
    private IntervalParseService intervalParseService;

    @InjectMocks
    private CsRequestValidationImpl csRequestValidation;  // class under test

    private CsRequest baseRequest(long openTime, long closeTime, String interval) {
        return new CsRequest(
                "BINANCE",
                "BTC-USDT",
                interval,
                openTime,
                closeTime
        );
    }

    @Test
    void validateCsRequest() {
        long openTime = 0L;
        long closeTime = 60_000L - 1;
        CsRequest csRequest = baseRequest(openTime, closeTime, "1m");

        when(marketDataService.getMarketId(csRequest.getExchange(), csRequest.getSymbol())).thenReturn(1L);
        when(exchangeServices.containsKey("BINANCE")).thenReturn(true);
        when(intervalParseService.toMillis("1m")).thenReturn(60_000L);

        CsParam csParam = csRequestValidation.validateCsRequest(csRequest);


        assertThat(csParam.getMarketId()).isEqualTo(1L);
        assertThat(csParam.getIntervalMs()).isEqualTo(60_000L);
        assertThat(csParam.getLimit()).isEqualTo(1L);
        assertThat(csParam.getOpenTime()).isEqualTo(openTime);
        assertThat(csParam.getCloseTime()).isEqualTo(closeTime);

        verify(marketDataService).getMarketId("BINANCE", "BTC-USDT");
        verify(intervalParseService).toMillis("1m");
    }

    @Test
    void invalidParameter_openTimeCloseTime() {
        CsRequest csRequest = baseRequest(2000L, 1000L, "1m");

        assertThatThrownBy(() -> csRequestValidation.validateCsRequest(csRequest))
                .isInstanceOf(CsRequestException.class)
                .hasMessageContaining("CloseTime must be after openTime");
    }

    @Test
    void invalidParameter_timeRange() {
        long openTime = 0L;
        long closeTime = 1_000L;
        CsRequest csRequest = baseRequest(openTime, closeTime, "1m");

        assertThatThrownBy(() -> csRequestValidation.validateCsRequest(csRequest))
                .isInstanceOf(CsRequestException.class)
                .hasMessageContaining("Time range smaller than the minimum interval");
    }

    @Test
    void invalidInterval() {
        long openTime = 0L;
        long closeTime = 60_000L - 1;
        CsRequest csRequest = baseRequest(openTime, closeTime, "5x");

        assertThatThrownBy(() -> csRequestValidation.validateCsRequest(csRequest))
                .isInstanceOf(CsRequestException.class)
                .hasMessageContaining("Valid intervals: m, h, w, M, y");
    }

    @Test
    void symbolUnavailable() {
        long openTime = 0L;
        long closeTime = 60_000L - 1;
        CsRequest csRequest = baseRequest(openTime, closeTime, "1m");

        when(marketDataService.getMarketId(csRequest.getExchange(), csRequest.getSymbol())).thenReturn(null);

        assertThatThrownBy(() -> csRequestValidation.validateCsRequest(csRequest))
                .isInstanceOf(CsRequestException.class)
                .hasMessageContaining("BTC-USDT on BINANCE");
    }

    @Test
    void exchangeUnavailable() {
        long openTime = 0L;
        long closeTime = 60_000L - 1;
        CsRequest csRequest = baseRequest(openTime, closeTime, "1m");

        when(marketDataService.getMarketId(csRequest.getExchange(), csRequest.getSymbol())).thenReturn(1L);
        when(exchangeServices.containsKey("BINANCE")).thenReturn(false);

        assertThatThrownBy(() -> csRequestValidation.validateCsRequest(csRequest))
                .isInstanceOf(CsRequestException.class)
                .hasMessageContaining("Exchange unavailable: BINANCE");
    }
}
