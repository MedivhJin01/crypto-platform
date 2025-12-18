package com.example.crypto_platform.ai.mongodb.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("news_history")
public class NewsHistory {

    @Id
    private String id;

    private String symbol;
    private String url;
    private String title;
    private String content;

    private LocalDate date;

    private boolean direction;
    private double change;

}
