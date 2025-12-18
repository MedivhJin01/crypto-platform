package com.example.crypto_platform.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@AllArgsConstructor
@NoArgsConstructor
public class MarketEvent {
    @NotNull @NotBlank private String symbol;
    @NotNull private long startTime;
    @NotNull private long endTime;
    @NotNull private boolean direction;
    @NotNull private double change;
}
