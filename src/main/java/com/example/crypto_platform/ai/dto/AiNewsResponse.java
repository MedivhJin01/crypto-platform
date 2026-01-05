package com.example.crypto_platform.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiNewsResponse {
    String symbol;
    String summary;
    Map<String, String> linksByTitle;
    String updateAt;
}
