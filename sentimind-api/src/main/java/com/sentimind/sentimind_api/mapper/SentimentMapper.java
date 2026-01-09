package com.sentimind.sentimind_api.mapper;

import java.time.LocalDateTime;

import com.sentimind.sentimind_api.dto.SentimentRequest;
import com.sentimind.sentimind_api.dto.SentimentResponse;
import com.sentimind.sentimind_api.model.SentimentAnalysis;

public class SentimentMapper {

    // Convierte el DTO de entrada en la Entidad para la DB
    public static SentimentAnalysis toEntity(SentimentRequest request, String sentiment, double confidence) {
    if (request == null) return null;

    SentimentAnalysis entity = new SentimentAnalysis();
    // NOTA: Se usa .text() en lugar de .getText() porque es un Record
    entity.setText(request.text()); 
    entity.setSentiment(sentiment);
    entity.setConfidence(confidence);
    return entity;
}

public static SentimentResponse toResponse(SentimentAnalysis entity) {
    if (entity == null) return null;

    // Los Records se instancian pasando todos los argumentos al constructor
    return new SentimentResponse(
    entity.getId(),
    entity.getSentiment(),
    entity.getConfidence(),
    entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now(), entity.getText()
);
}
}