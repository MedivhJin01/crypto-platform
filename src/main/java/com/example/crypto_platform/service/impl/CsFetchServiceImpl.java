package com.example.crypto_platform.service.impl;

import com.example.crypto_platform.dto.CsParam;
import com.example.crypto_platform.model.Candlestick;
import com.example.crypto_platform.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


import java.util.*;
import java.util.stream.LongStream;

@Service
@Validated
public class CsFetchServiceImpl implements CsFetchService {

    @Autowired
    private MarketDataService marketDataService;
    @Autowired
    private Map<String, ExchangeService> exchangeServices;


    @Override
    public void fetch(CsParam csParam){
        final ExchangeService exchangeService = exchangeServices.get(csParam.getExchange().toUpperCase());
        final long granMs = csParam.getIntervalMs();
        final int PAGE_LIMIT = 300;
        final long CHUNK_MS = PAGE_LIMIT * granMs;
        final long startTime = csParam.getOpenTime();
        final long endTime = csParam.getCloseTime();

        LongStream.iterate(startTime, t -> t <= endTime, t -> t + CHUNK_MS)
                .unordered()
                .parallel()
                .forEach(pageOpenTime -> {
                    long pageCloseTime = Math.min(pageOpenTime + CHUNK_MS - 1, endTime);
                    int pageLimit = (int) ((pageCloseTime - pageOpenTime) / granMs + 1);
                    CsParam pageParam = new CsParam(
                            csParam.getMarketId(),
                            csParam.getIntervalMs(),
                            pageLimit,
                            csParam.getExchange(),
                            csParam.getSymbol(),
                            csParam.getInterval(),
                            pageOpenTime,
                            pageCloseTime
                    );

                Map<String, ?> httpParams = exchangeService.buildHttpParams(pageParam);
                List<Candlestick> rows = exchangeService.fetchCsData(pageParam, httpParams);
                marketDataService.insert(rows);
            });
    }
}
