package com.example.crypto_platform.backend.controller;

import com.example.crypto_platform.backend.dto.CsParam;
import com.example.crypto_platform.backend.dto.CsRequest;
import com.example.crypto_platform.backend.model.Candlestick;
import com.example.crypto_platform.backend.service.CsGetService;
import com.example.crypto_platform.backend.service.IntervalParseService;
import com.example.crypto_platform.backend.validation.CsRequestValidation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/candlestick")
@Tag(name = "Candlestick", description = "CsController Endpoints")
public class CsController {
    @Autowired
    private CsRequestValidation csRequestValidation;
    @Autowired
    private CsGetService csGetService;
    @Autowired
    private IntervalParseService intervalParseService;

    @GetMapping("/get/aggregated")
    @ResponseStatus(HttpStatus.OK)
    public List<Candlestick> getAggCss(@Validated CsRequest CsRequest){
        CsParam csParam = csRequestValidation.validateCsRequest(CsRequest);
        return csGetService.getAggCss(csParam);
    }

    @GetMapping("/get/latest/{symbol}/{interval}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Candlestick> getLatestCssBySymbol(@PathVariable String symbol, @PathVariable String interval){
        long intervalMs = intervalParseService.toMillis(interval);
        return csGetService.getLatestCss(symbol, intervalMs);
    }

}
