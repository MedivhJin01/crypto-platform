package com.example.crypto_platform.mapper;

import com.example.crypto_platform.model.Market;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MarketMapper {

    @Select("""
        SELECT
            id                  AS id,
            exchange_id         AS exchangeId,
            base_asset_id       AS baseAssetId,
            quote_asset_id      AS quoteAssetId,
            symbol              AS symbol,
            exchange_name       AS exchangeName
        FROM market
        WHERE symbol = #{symbol}
    """)
    List<Market> getMarketsBySymbol(@Param("symbol") String symbol);

}
