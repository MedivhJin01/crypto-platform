package com.example.crypto_platform.backend.service.impl;

import com.example.crypto_platform.backend.dto.CsParam;
import com.example.crypto_platform.backend.model.Candlestick;
import com.example.crypto_platform.backend.model.Market;
import com.example.crypto_platform.backend.service.IntervalParseService;
import com.example.crypto_platform.backend.service.LockService;
import com.example.crypto_platform.backend.service.MarketDataService;
import com.example.crypto_platform.backend.service.RedisService;
import com.example.crypto_platform.backend.utils.LockCallbackUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsGetServiceImplTests {

    @Mock
    private MarketDataService marketDataService;

    @Mock
    private IntervalParseService intervalParseService;

    @Mock
    private RedisService redisService;

    @Mock
    private LockService lockService;

    @InjectMocks
    private CsGetServiceImpl csGetService;

    private static CsParam sampleAggParam(long startTime, long endTime, long intervalMs, int limit) {
        return new CsParam(
                1L,
                intervalMs,
                limit,
                "BINANCE",
                "BTC-USDT",
                intervalMs == 300_000L ? "5m" : "1m",
                startTime,
                endTime
        );
    }

    private static List<Candlestick> make1mCandles(int n) {
        List<Candlestick> css = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            css.add(new Candlestick(
                    1L,
                    60_000L,
                    (long) i * 60_000L,
                    (long) i * 60_000L + 59_999L,
                    BigDecimal.valueOf(i),           // open
                    BigDecimal.valueOf(i + 1),       // high
                    BigDecimal.valueOf(i),           // low
                    BigDecimal.valueOf(i + 0.5),     // close
                    BigDecimal.ONE                   // vol
            ));
        }
        return css;
    }



    @Test
    void getAggCssTest_cacheHit() {
        CsParam csParam = sampleAggParam(0L, 600_000L, 300_000L, 2);
        List<Candlestick> cached = List.of(
                new Candlestick(1L, 300_000L, 0L, 299_999L,
                        BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE),
                new Candlestick(1L, 300_000L, 300_000L, 599_999L,
                        BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE)
        );
        when(redisService.getAggCss(eq(csParam))).thenReturn(cached);
        List<Candlestick> result = csGetService.getAggCss(csParam);

        assertThat(result).isSameAs(cached);
        verify(redisService, times(1)).getAggCss(eq(csParam));
        verifyNoInteractions(lockService);
        verifyNoInteractions(marketDataService);
        verifyNoInteractions(intervalParseService);
    }

    @Test
    void getAggCssTest_cacheMiss_lock_save() {
        long startTime = 0L;
        long endTime = 600_000;
        CsParam csParam = sampleAggParam(startTime, endTime, 300_000L, 2);
        when(redisService.getAggCss(eq(csParam))).thenReturn(List.of());
        when(intervalParseService.toMillis(eq("1m"))).thenReturn(60_000L);
        when(marketDataService.getMarketData(any(CsParam.class))).thenReturn(make1mCandles(10));

        when(lockService.executeWithLock(anyString(), anyLong(), anyLong(), any(LockCallbackUtil.class)))
                .thenAnswer(inv -> {
                    @SuppressWarnings("unchecked")
                    LockCallbackUtil<List<Candlestick>> cb = inv.getArgument(3);
                    return cb.doWithLock();
                });
        List<Candlestick> result = csGetService.getAggCss(csParam);
        assertThat(result).hasSize(2);

        Candlestick c0 = result.stream().filter(c -> c.getOpenTime() == 0L).findFirst().orElseThrow();
        assertThat(c0.getCloseTime()).isEqualTo(299_999L);
        assertThat(c0.getOpenPrice()).isEqualByComparingTo(BigDecimal.valueOf(0));
        assertThat(c0.getHighPrice()).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(c0.getLowPrice()).isEqualByComparingTo(BigDecimal.valueOf(0));
        assertThat(c0.getClosePrice()).isEqualByComparingTo(BigDecimal.valueOf(4.5));
        assertThat(c0.getVolume()).isEqualByComparingTo(BigDecimal.valueOf(5));

        // bucket 1: candles 5..9
        Candlestick c1 = result.stream().filter(c -> c.getOpenTime() == 300_000L).findFirst().orElseThrow();
        assertThat(c1.getCloseTime()).isEqualTo(599_999L);
        assertThat(c1.getOpenPrice()).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(c1.getHighPrice()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(c1.getLowPrice()).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(c1.getClosePrice()).isEqualByComparingTo(BigDecimal.valueOf(9.5));
        assertThat(c1.getVolume()).isEqualByComparingTo(BigDecimal.valueOf(5));

        verify(redisService, times(2)).getAggCss(eq(csParam)); // before lock, and inside lock
        verify(redisService, times(1)).saveAggCss(eq(csParam), anyList());
        verify(intervalParseService, times(1)).toMillis("1m");
        verify(marketDataService, times(1)).getMarketData(any(CsParam.class));
        verify(lockService, times(1)).executeWithLock(anyString(), anyLong(), anyLong(), any(LockCallbackUtil.class));
    }

    @Test
    void getAggCssTest_lockReturnsNull_fallsBackToRedis() {
        CsParam csParam = sampleAggParam(0L, 600_000L, 300_000L, 2);

        when(redisService.getAggCss(eq(csParam)))
                .thenReturn(List.of())
                // fallback read after sleep
                .thenReturn(List.of(
                        new Candlestick(1L, 300_000L, 0L, 299_999L,
                                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE),
                        new Candlestick(1L, 300_000L, 300_000L, 599_999L,
                                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE)
                ));

        // lock returns null (could also return empty list)
        when(lockService.executeWithLock(anyString(), anyLong(), anyLong(), any(LockCallbackUtil.class)))
                .thenReturn(null);

        List<Candlestick> result = csGetService.getAggCss(csParam);

        assertThat(result).hasSize(2);

        verify(redisService, times(2)).getAggCss(eq(csParam));
        verify(lockService, times(1)).executeWithLock(anyString(), anyLong(), anyLong(), any(LockCallbackUtil.class));
        verifyNoInteractions(intervalParseService);
        verifyNoInteractions(marketDataService);
    }

    @Test
    void getLatestCssTest() {
        String symbol = "BTC-USDT";
        long intervalMs = 60_000L;

        Market m1 = mock(Market.class);
        when(m1.getId()).thenReturn(1L);
        when(m1.getExchangeName()).thenReturn("BINANCE");

        Market m2 = mock(Market.class);
        when(m2.getId()).thenReturn(2L);
        when(m2.getExchangeName()).thenReturn("OKX");

        when(marketDataService.getMarketsBySymbol(eq(symbol))).thenReturn(List.of(m1, m2));

        // one exists, one missing
        Candlestick c0 = new Candlestick(
                1L, intervalMs,
                123_000L, 123_000L + intervalMs - 1,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE
        );

        Candlestick c1 = new Candlestick(
                2L, intervalMs,
                123_000L, 123_000L + intervalMs - 1,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE
        );

        when(redisService.getLatestCs(eq(1L), eq(intervalMs), anyLong())).thenReturn(c0);
        when(redisService.getLatestCs(eq(2L), eq(intervalMs), anyLong())).thenReturn(c1);

        Map<String, Candlestick> result = csGetService.getLatestCss(symbol, intervalMs);

        assertThat(result).containsKey("BINANCE");
        assertThat(result).containsKey("OKX");
        assertThat(result.get("BINANCE")).isSameAs(c0);
        assertThat(result.get("OKX")).isSameAs(c1);


        verify(marketDataService, times(1)).getMarketsBySymbol(symbol);
        verify(redisService, times(1)).getLatestCs(eq(1L), eq(intervalMs), anyLong());
        verify(redisService, times(1)).getLatestCs(eq(2L), eq(intervalMs), anyLong());
    }



}