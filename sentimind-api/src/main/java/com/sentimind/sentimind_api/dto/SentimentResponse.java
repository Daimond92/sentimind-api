package com.sentimind.sentimind_api.dto;

import java.time.LocalDateTime;

public record SentimentResponse(
    Long id,
    String sentiment,
    double confidence,
    LocalDateTime timestamp,
    String text
){}
