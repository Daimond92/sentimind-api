package com.sentimind.sentimind_api.dto;

public record AiModelResponse(
    String sentiment,
    double confidence
) {}