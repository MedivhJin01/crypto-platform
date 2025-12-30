package com.example.crypto_platform.backend;

import com.example.crypto_platform.backend.dto.CsParam;
import com.example.crypto_platform.backend.dto.CsRequest;
import com.example.crypto_platform.backend.mapper.MarketDataMapper;
import com.example.crypto_platform.backend.service.CsFetchService;
import com.example.crypto_platform.backend.service.IntervalParseService;
import com.example.crypto_platform.backend.validation.CsRequestValidation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@AutoConfigureMockMvc
class CryptoPlatformBackendTests {

    @Autowired
    private MarketDataMapper marketDataMapper;
    @Autowired
    private IntervalParseService intervalParseService;
    @Autowired
    private CsRequestValidation csRequestValidation;
    @Autowired
    private CsFetchService csFetchService;

//    @Test
//    void cleanDb() {
//        // ensure a clean state for every test
//        marketDataMapper.deleteAll();
//        assertThat(marketDataMapper.countAll()).isZero();
//    }


//    @Test
//    void fetchCsTest() throws Exception {
//        marketDataMapper.deleteAll();
//        assertThat(marketDataMapper.countAll()).isZero();
//
//
//        long endTime = 1765253159999L;
//        long startTime = endTime + 1L - intervalParseService.toMillis("1d");
//
//        CsRequest csRequest = new CsRequest(
//                "BYBIT",
//                "BTC-USDT",
//                "1m",
//                startTime,
//                endTime
//        );
//
//        CsParam csParam = csRequestValidation.validateCsRequest(csRequest);
//        csFetchService.fetch(csParam);
//
//        long rows = marketDataMapper.countAll();
//    }

}
