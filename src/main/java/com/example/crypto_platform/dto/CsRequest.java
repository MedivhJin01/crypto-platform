package com.example.crypto_platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CsRequest {
    @NotBlank private String exchange;
    @NotBlank private String symbol;
    @NotBlank private String interval;

    @NotNull @Positive private Long openTime;
    @NotNull @Positive private Long closeTime;
}
