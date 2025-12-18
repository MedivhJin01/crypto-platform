package com.example.crypto_platform.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TavilyResponse {

    @JsonProperty("query")
    private String query;

    @JsonProperty("results")
    private List<Result> results;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {

        @JsonProperty("url")
        private String url;

        @JsonProperty("title")
        private String title;

        @JsonProperty("content")
        private String content;

    }
}
