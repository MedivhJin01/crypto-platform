package com.example.crypto_platform.service.impl;

import com.example.crypto_platform.dto.CsParam;
import com.example.crypto_platform.model.Candlestick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
public class RedisServiceImplTests {

    @Mock
    private RedisTemplate<String, Candlestick> redisTemplate;

    @Mock
    private ZSetOperations<String, Candlestick> zSetOperations;

    @Mock
    private ValueOperations<String, Candlestick> valueOperations;

    @InjectMocks
    private RedisServiceImpl redisServiceImpl;


    @Test
    void saveLatestCsTest() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Candlestick cs = new Candlestick(
                1L, 60_000L,
                0L, 59_999L,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE
        );

        redisServiceImpl.saveLatestCs(cs);

        String expectedKey = "latest:1:60000";
        verify(valueOperations).set(eq(expectedKey), eq(cs), eq(1L), eq(TimeUnit.MINUTES));
    }

    @Test
    void getLatestCsTest() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Long marketId = 1L;
        Long intervalMs = 60_000L;

        Candlestick expected = new Candlestick(
                1L, 60_000L,
                0L, 59_999L,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE
        );

        String expectedKey = "latest:1:60000";
        when(valueOperations.get(expectedKey)).thenReturn(expected);
        Candlestick actual = redisServiceImpl.getLatestCs(marketId, intervalMs, 0L);
        assertEquals(expected, actual);
        verify(valueOperations).get(expectedKey);
    }

    @Test
    public void saveAggCssTest() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        CsParam csParam = new CsParam();
        csParam.setMarketId(1L);
        csParam.setIntervalMs(60_000L);

        Candlestick candlestick_1 = new Candlestick(
                1L,
                60_000L,
                0L,
                59_999L,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(1),
                BigDecimal.ONE
        );
        Candlestick candlestick_2 = new Candlestick(
                1L,
                60_000L,
                60_000L,
                119_999L,
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.ONE
        );

        List<Candlestick> candlesticks = List.of(candlestick_1, candlestick_2);
        redisServiceImpl.saveAggCss(csParam, candlesticks);

        String expectedKey = "series:1:60000";
        ArgumentCaptor<Set<ZSetOperations.TypedTuple<Candlestick>>> captor =
                ArgumentCaptor.forClass(Set.class);

        verify(zSetOperations).add(eq(expectedKey), captor.capture());
        Set<ZSetOperations.TypedTuple<Candlestick>> tuples = captor.getValue();
        assertEquals(2, tuples.size());

        boolean hasCs1 = tuples.stream().anyMatch(t ->
                t.getValue() == candlestick_1 && t.getScore() == 0.0);
        boolean hasCs2 = tuples.stream().anyMatch(t ->
                t.getValue() == candlestick_2 && t.getScore() == 60000.0);

        assertTrue(hasCs1);
        assertTrue(hasCs2);
    }

    @Test
    public void getAggTest() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        CsParam csParam = new CsParam();
        csParam.setMarketId(1L);
        csParam.setIntervalMs(60_000L);
        csParam.setOpenTime(0L);
        csParam.setCloseTime(119_999L);

        String key = "series:1:60000";
        Candlestick candlestick_1 = new Candlestick(
                1L,
                60_000L,
                0L,
                59_999L,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(1),
                BigDecimal.ONE
        );
        Candlestick candlestick_2 = new Candlestick(
                1L,
                60_000L,
                60_000L,
                119_999L,
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.ONE
        );

        Set<Candlestick> res = new LinkedHashSet<>();
        res.add(candlestick_1);
        res.add(candlestick_2);

        when(zSetOperations.rangeByScore(key, 0L, 119_999L))
                .thenReturn(res);
        List<Candlestick> result = redisServiceImpl.getAggCss(csParam);

        // then
        assertEquals(2, result.size());
        assertEquals(candlestick_1, result.get(0));
        assertEquals(candlestick_2, result.get(1));

        // verify correct Redis call
        verify(zSetOperations).rangeByScore(key, 0L, 119_999L);
    }

}
