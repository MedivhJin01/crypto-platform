package com.example.crypto_platform.backend.dto;

import com.example.crypto_platform.backend.model.Candlestick;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CsBatch {
    private List<Candlestick> css;
}
