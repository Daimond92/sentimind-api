package com.sentimind.sentimind_api.service.impl;

import com.sentimind.sentimind_api.dto.SentimentRequest;
import com.sentimind.sentimind_api.dto.SentimentResponse;
import com.sentimind.sentimind_api.dto.AiModelResponse;
import com.sentimind.sentimind_api.model.SentimentAnalysis;
import com.sentimind.sentimind_api.repository.SentimentRepository;
import com.sentimind.sentimind_api.service.SentimentService;
import com.sentimind.sentimind_api.mapper.SentimentMapper;
import com.sentimind.sentimind_api.client.SentimentClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SentimentServiceImpl implements SentimentService {

    @Value("${ai.integration.enabled:false}")
    private boolean isAiEnabled;

    private final SentimentClient aiClient;
    private final SentimentRepository repository;
    private final SentimentMapper mapper;

    public SentimentServiceImpl(SentimentClient aiClient, SentimentRepository repository, SentimentMapper mapper) {
        this.aiClient = aiClient;
        this.repository = repository;
        this.mapper = mapper;
    }

    private String simulateSentiment(String text) {
        String lowerText = text.toLowerCase();
        if (lowerText.contains("excelente") || lowerText.contains("bueno") || lowerText.contains("maravilloso")) {
            return "Positivo";
        } else if (lowerText.contains("malo") || lowerText.contains("terrible") || lowerText.contains("horrible")) {
            return "Negativo";
        } else {
            return "Neutral";
        }
    }

    @Override
    public SentimentResponse analyzeSentiment(SentimentRequest request) {
        String sentiment;
        double confidence;

        if (isAiEnabled) {
            // Ahora 'prediction' es un AiModelResponse
            AiModelResponse prediction = aiClient.getAiPrediction(request.text());
            
            // Accedemos directamente a los campos del Record
            sentiment = prediction.sentiment();
            confidence = prediction.confidence();
        } else {
            sentiment = simulateSentiment(request.text());
            confidence = 0.95;
        }

        SentimentAnalysis entity = mapper.toEntity(request, sentiment, confidence);

        return mapper.toResponse(repository.save(entity));
    }
}