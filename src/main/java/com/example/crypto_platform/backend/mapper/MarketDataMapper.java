package com.example.crypto_platform.backend.mapper;

import com.example.crypto_platform.backend.dto.CsParam;
import com.example.crypto_platform.backend.model.Candlestick;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MarketDataMapper {

    /**
     * Insert a batch of Candlestick records.
     */
    @Insert({
        "<script>",
            "INSERT IGNORE INTO market_data (",
            "    market_id,",
            "    kline_interval, open_time, close_time,",
            "    open_price, high_price, low_price, close_price, volume",
            ") VALUES",
            "<foreach collection='cs' item='c' separator=','>",
            "    (#{c.marketId},",
            "     #{c.klineInterval}, #{c.openTime}, #{c.closeTime},",
            "     #{c.openPrice}, #{c.highPrice}, #{c.lowPrice}, #{c.closePrice},",
            "     #{c.volume})",
            "</foreach>",
        "</script>"
    })
    int insertBatch(@Param("cs") List<Candlestick> CsRaws);

    /**
     * Count the total number of rows in the market_kline table.
     */
    @Select("SELECT COUNT(*) FROM market_data")
    long countAll();

    /**
     * Delete all klines from the table.
     */
    @Delete("DELETE FROM market_data")
    int deleteAll();

    @Select("""
        SELECT
            market_id        AS marketId,
            kline_interval   AS klineInterval,
            open_time        AS openTime,
            close_time       AS closeTime,
            open_price       AS openPrice,
            high_price       AS highPrice,
            low_price        AS lowPrice,
            close_price      AS closePrice,
            volume           AS volume
        FROM market_data
        WHERE market_id = #{p.marketId}
          AND kline_interval = #{p.intervalMs}
          AND open_time BETWEEN #{p.openTime} AND #{p.closeTime}
        ORDER BY open_time ASC
    """)
    List<Candlestick> get(@Param("p") CsParam csParam);


    @Select("""
        SELECT m.id
        FROM market m
        JOIN exchange e ON m.exchange_id = e.id
        WHERE e.name = #{exchange}
          AND m.symbol = #{symbol}
    """)
    Long getMarketId(@Param("exchange") String exchange, @Param("symbol") String symbol);

    @Select("""
        SELECT MAX(close_time)
        FROM market_data
        WHERE market_id = #{marketId}
        AND kline_interval = #{intervalMs}
    """)
    Long getLastCsCloseTime(@Param("marketId") Long marketId, @Param("intervalMs") Long intervalMs);

    @Select("""
        SELECT
            market_id        AS marketId,
            kline_interval   AS klineInterval,
            open_time        AS openTime,
            close_time       AS closeTime,
            open_price       AS openPrice,
            high_price       AS highPrice,
            low_price        AS lowPrice,
            close_price      AS closePrice,
            volume           AS volume
        FROM market_data
        WHERE market_id = #{marketId}
        AND kline_interval = #{intervalMs}
        ORDER BY close_time DESC
        LIMIT 1
    """)
    Candlestick getLastCsByMarketId(@Param("marketId") Long marketId, @Param("intervalMs") Long intervalMs);
}