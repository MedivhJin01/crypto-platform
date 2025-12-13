package com.example.crypto_platform.dto.exchangeResponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OKXResponse {
    private String code;
    private String msg;

    @JsonProperty("data")
    private List<List<String>> data;
}
