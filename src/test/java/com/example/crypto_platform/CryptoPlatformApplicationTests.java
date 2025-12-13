package com.example.crypto_platform;

import com.example.crypto_platform.dto.CsParam;
import com.example.crypto_platform.dto.CsRequest;
import com.example.crypto_platform.mapper.MarketDataMapper;
import com.example.crypto_platform.service.CsFetchService;
import com.example.crypto_platform.service.IntervalParseService;
import com.example.crypto_platform.validation.CsRequestValidation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@AutoConfigureMockMvc
class CryptoPlatformApplicationTests {

    @Autowired
    private MarketDataMapper marketDataMapper;
    @Autowired
    private IntervalParseService intervalParseService;
    @Autowired
    private CsRequestValidation csRequestValidation;
    @Autowired
    private CsFetchService csFetchService;

    @Test
    void cleanDb() {
        // ensure a clean state for every test
        marketDataMapper.deleteAll();
        assertThat(marketDataMapper.countAll()).isZero();
    }


    @Test
    void fetchCsTest() throws Exception {
        marketDataMapper.deleteAll();
        assertThat(marketDataMapper.countAll()).isZero();


        long endTime = 1765253159999L;
        long startTime = endTime + 1L - intervalParseService.toMillis("1d");

        CsRequest csRequest = new CsRequest(
                "BYBIT",
                "BTC-USDT",
                "1m",
                startTime,
                endTime
        );

        CsParam csParam = csRequestValidation.validateCsRequest(csRequest);
        csFetchService.fetch(csParam);

        long rows = marketDataMapper.countAll();
    }


//    @Test
//    void getAggCssTest() throws Exception {
//
//        long startTime = 1764648360000L;
//        long endTime = 1765253159999L;
//
////        System.out.println("startTime = " + startTime);
//        CsRequest csRequest = new CsRequest(
//                "BINANCE",
//                "BTC-USDT",
//                "5m",
//                startTime,
//                endTime
//        );
//
//        long t0 = System.nanoTime();
//
//        String responseJson = mockMvc.perform(get("/candlestick/get/aggregated")
//                        .param("exchange", "BINANCE")
//                        .param("symbol", "BTC-USDT")
//                        .param("interval", "5m")
//                        .param("openTime", String.valueOf(startTime))
//                        .param("closeTime", String.valueOf(endTime)))
//                .andExpect(status().isOk())
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//
//
//
//        long t1 = System.nanoTime();
//        long elapsedMs = (t1 - t0) / 1_000_000;
//        System.out.println("get /candlestick/get/aggregated took " + elapsedMs + " ms");
//
//        List<Candlestick> candlesticks = objectMapper.readValue(
//                responseJson,
//                objectMapper.getTypeFactory().constructCollectionType(List.class, Candlestick.class)
//        );
//
//        System.out.println(candlesticks.size());
////        assertThat(candlesticks.size()).isEqualTo(288);
//        marketDataMapper.deleteAll();
//        assertThat(marketDataMapper.countAll()).isZero();
//    }



}
