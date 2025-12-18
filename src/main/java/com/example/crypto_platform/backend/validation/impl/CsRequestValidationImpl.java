package com.example.crypto_platform.backend.validation.impl;

import com.example.crypto_platform.backend.dto.CsParam;
import com.example.crypto_platform.backend.dto.CsRequest;
import com.example.crypto_platform.backend.exception.CsRequestException;
import com.example.crypto_platform.backend.service.ExchangeService;
import com.example.crypto_platform.backend.service.IntervalParseService;
import com.example.crypto_platform.backend.service.MarketDataService;
import com.example.crypto_platform.backend.validation.CsRequestValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CsRequestValidationImpl implements CsRequestValidation {

    @Autowired
    private MarketDataService marketDataService;

    @Autowired
    private Map<String, ExchangeService> exchangeServices;

    @Autowired
    private IntervalParseService intervalParseService;

    private static final String INTERVAL_REGEX = "^\\d+[mhwMy]$";

    @Override
    public CsParam validateCsRequest(CsRequest csRequest) {
        long openTime = csRequest.getOpenTime();
        long closeTime = csRequest.getCloseTime();
        String interval = csRequest.getInterval();
        String exchange = csRequest.getExchange();
        String symbol = csRequest.getSymbol();

        if (closeTime < openTime) {
            throw CsRequestException.invalidParameter("CloseTime must be after openTime");
        }
        if (closeTime - openTime + 1 < 60 * 1_000L) {
            throw CsRequestException.invalidParameter("Time range smaller than the minimum interval");
        }
        if (!interval.matches(INTERVAL_REGEX)) {
            throw CsRequestException.invalidInterval("Valid intervals: m, h, w, M, y");
        }

        Long marketId = marketDataService.getMarketId(exchange, symbol);
        if (marketId == null) {
            throw CsRequestException.marketDataUnavailable(symbol + " on " + exchange);
        }
        if (!exchangeServices.containsKey(exchange.toUpperCase())) {
            throw CsRequestException.exchangeUnavailable(exchange);
        }

        long intervalMs = intervalParseService.toMillis(interval);
        int limit = (int) ((closeTime - openTime + 1) / intervalMs);

        return new CsParam(
                marketId,
                intervalMs,
                limit,
                exchange,
                symbol,
                interval,
                openTime,
                closeTime
        );
    }

}
