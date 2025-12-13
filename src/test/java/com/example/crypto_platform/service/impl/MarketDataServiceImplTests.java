package com.example.crypto_platform.service.impl;

import com.example.crypto_platform.dto.CsParam;
import com.example.crypto_platform.mapper.MarketDataMapper;
import com.example.crypto_platform.model.Candlestick;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MarketDataServiceImplTests {
    @Mock
    private MarketDataMapper marketDataMapper;

    @InjectMocks
    private MarketDataServiceImpl marketDataServiceImpl;

    @Test
    void insert() {
        CsParam csParam = new CsParam(
                1L,
                60_000L,
                3,
                "BINANCE",
                "BTC-USDT",
                "1m",
                1000L,
                2000L

        );

        List<Candlestick> css = List.of(
                new Candlestick(
                        1L,
                        60_000L,
                        1000L,
                        1059L,
                        new BigDecimal("1.0"),
                        new BigDecimal("2.0"),
                        new BigDecimal("0.5"),
                        new BigDecimal("1.5"),
                        new BigDecimal("10")
                ),
                new Candlestick(
                        1L,
                        60_000L,
                        1060L,
                        1119L,
                        new BigDecimal("1.0"),
                        new BigDecimal("2.0"),
                        new BigDecimal("0.5"),
                        new BigDecimal("1.5"),
                        new BigDecimal("10")
                )
        );
        when(marketDataMapper.insertBatch(css)).thenReturn(2);

        int inserted = marketDataServiceImpl.insert(css);

        assertThat(inserted).isEqualTo(2);
        verify(marketDataMapper, times(1)).insertBatch(css);
        verifyNoMoreInteractions(marketDataMapper);
    }

    @Test
    void get() {
        CsParam csParam = new CsParam(
                1L,
                60_000L,
                1,
                "BINANCE",
                "BTC-USDT",
                "1m",
                1000L,
                2000L
        );
        when(marketDataMapper.get(csParam)).thenReturn(null);
        List<Candlestick> get = marketDataServiceImpl.getMarketData(csParam);
        assertThat(get).isNull();
        verify(marketDataMapper, times(1)).get(csParam);
        verifyNoMoreInteractions(marketDataMapper);
    }

    @Test
    void getMarketId() {
        CsParam csParam = new CsParam(
                1L,
                60_000L,
                1,
                "BINANCE",
                "BTC-USDT",
                "1m",
                1000L,
                2000L
        );
        when(marketDataMapper.getMarketId(csParam.getExchange(), csParam.getSymbol())).thenReturn(1L);
        Long marketId = marketDataServiceImpl.getMarketId(csParam.getExchange(), csParam.getSymbol());
        assertThat(marketId).isNotNull();
        verify(marketDataMapper, times(1)).getMarketId(csParam.getExchange(), csParam.getSymbol());
        verifyNoMoreInteractions(marketDataMapper);
    }

    @Test
    void deleteAll() {
        when(marketDataMapper.deleteAll()).thenReturn(1);
        int deleted = marketDataServiceImpl.deleteAll();
        assertThat(deleted).isEqualTo(1);
        verify(marketDataMapper, times(1)).deleteAll();
        verifyNoMoreInteractions(marketDataMapper);
    }

    @Test
    void countAll() {
        when(marketDataMapper.countAll()).thenReturn(1L);
        long count = marketDataServiceImpl.countAll();
        assertThat(count).isEqualTo(1);
        verify(marketDataMapper, times(1)).countAll();
        verifyNoMoreInteractions(marketDataMapper);
    }
}

