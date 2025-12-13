package com.example.crypto_platform.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CsParam {
    @NotNull private Long marketId;
    @NotNull private Long intervalMs;
    @NotNull @Positive private Integer limit;

    @NotNull private String exchange;
    @NotNull private String symbol;
    @NotNull private String interval;

    @NotNull @Positive private Long openTime;
    @NotNull @Positive private Long closeTime;
}

