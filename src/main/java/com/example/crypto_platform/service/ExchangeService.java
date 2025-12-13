package com.example.crypto_platform.service;

import com.example.crypto_platform.dto.CsParam;
import com.example.crypto_platform.model.Candlestick;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Validated
public interface ExchangeService {

    Map<String, ?> buildHttpParams(@NotNull CsParam csParam);

    @NotEmpty
    List<@NotNull @Valid Candlestick> fetchCsData(@NotNull CsParam csParam, @NotNull Map<String, ?> params);

}
