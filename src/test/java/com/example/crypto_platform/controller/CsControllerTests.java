package com.example.crypto_platform.controller;

import com.example.crypto_platform.dto.CsParam;
import com.example.crypto_platform.dto.CsRequest;
import com.example.crypto_platform.model.Candlestick;
import com.example.crypto_platform.service.CsGetService;
import com.example.crypto_platform.service.CsFetchService;
import com.example.crypto_platform.service.IntervalParseService;
import com.example.crypto_platform.validation.CsRequestValidation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CsControllerTests {

    @Mock
    private IntervalParseService intervalParseService;

    @Mock
    private CsRequestValidation csRequestValidation;

    @Mock
    private CsGetService csGetService;

    @InjectMocks
    private CsController csController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // build MockMvc without starting a Spring context
        mockMvc = MockMvcBuilders.standaloneSetup(csController).build();
        objectMapper = new ObjectMapper();
    }

    private CsRequest sampleRequest() {
        return new CsRequest(
                "BINANCE",
                "BTC-USDT",
                "1m",
                1000L,
                2000L
        );
    }

    private CsParam sampleParam() {
        return new CsParam(
                1L,
                60_000L,
                1,
                "BINANCE",
                "BTC-USDT",
                "1m",
                1000L,
                2000L
        );
    }

    @Test
    void getAggCssTest() throws Exception {
        CsRequest csRequest = sampleRequest();
        CsParam csParam = sampleParam();

        List<Candlestick> resultList = List.of(
                new Candlestick(
                        1L, 60_000L,
                        1000L, 1059L,
                        BigDecimal.TEN, BigDecimal.valueOf(11),
                        BigDecimal.valueOf(9), BigDecimal.valueOf(10),
                        BigDecimal.ONE
                )
        );

        when(csRequestValidation.validateCsRequest(any(CsRequest.class)))
                .thenReturn(csParam);
        when(csGetService.getAggCss(any(CsParam.class)))
                .thenReturn(resultList);

        mockMvc.perform(
                        get("/candlestick/get/aggregated")
                                .param("exchange", "BINANCE")
                                .param("symbol", "BTC-USDT")
                                .param("interval", "1m")
                                .param("openTime", "1000")
                                .param("closeTime", "2000")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].marketId").value(1L))
                .andExpect(jsonPath("$[0].klineInterval").value(60_000L));

        verify(csRequestValidation, times(1)).validateCsRequest(any(CsRequest.class));
        verify(csGetService, times(1)).getAggCss(any(CsParam.class));
        verifyNoMoreInteractions(csRequestValidation, csGetService);
    }

    @Test
    void getLatestCssBySymbolTest() throws Exception {
        String symbol = "BTC-USDT";
        String interval = "1m";
        long intervalMs = 60_000L;

        Candlestick cs = new Candlestick(
                1L, intervalMs,
                123_000L, 123_000L + intervalMs - 1,
                BigDecimal.valueOf(10), BigDecimal.valueOf(11),
                BigDecimal.valueOf(9), BigDecimal.valueOf(10),
                BigDecimal.ONE
        );

        when(intervalParseService.toMillis(eq(interval))).thenReturn(intervalMs);
        when(csGetService.getLatestCss(eq(symbol), eq(intervalMs)))
                .thenReturn(Map.of("BINANCE", cs));

        mockMvc.perform(get("/candlestick/get/latest/{symbol}/{interval}", symbol, interval))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                // Map key = "BINANCE"
                .andExpect(jsonPath("$.BINANCE.marketId").value(1))
                .andExpect(jsonPath("$.BINANCE.klineInterval").value(60000))
                .andExpect(jsonPath("$.BINANCE.openTime").value(123000));

        verify(intervalParseService, times(1)).toMillis(interval);
        verify(csGetService, times(1)).getLatestCss(symbol, intervalMs);
        verifyNoMoreInteractions(intervalParseService, csGetService);
    }
}