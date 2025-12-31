package com.sentimind.sentimind_api.service.impl;

import com.sentimind.sentimind_api.dto.SentimentRequest;
import com.sentimind.sentimind_api.dto.SentimentResponse;
import com.sentimind.sentimind_api.model.SentimentAnalysis;
import com.sentimind.sentimind_api.repository.SentimentRepository;
import com.sentimind.sentimind_api.service.SentimentService;
import com.sentimind.sentimind_api.mapper.SentimentMapper; // Importas tu mapper
import org.springframework.stereotype.Service;

@Service
public class SentimentServiceImpl implements SentimentService {

    private final SentimentRepository sentimentRepository;

    public SentimentServiceImpl(SentimentRepository sentimentRepository) {
        this.sentimentRepository = sentimentRepository;
    }

    @Override
public SentimentResponse analyzeSentiment(SentimentRequest request) {
    String text = request.text().toLowerCase();
    
    String resultSentiment;
    double confidence;

    // 1. Lógica para POSITIVO
    if (text.contains("bueno") || text.contains("excelente") || text.contains("increíble") || text.contains("maravillosa")) {
        resultSentiment = "Positivo";
        confidence = 0.95;
    } 
    // 2. Lógica para NEGATIVO (Agregamos palabras clave negativas)
    else if (text.contains("malo") || text.contains("terrible") || text.contains("horrible") || text.contains("pésimo")) {
        resultSentiment = "Negativo";
        confidence = 0.90;
    } 
    // 3. Lógica para NEUTRO (Si no es ninguna de las anteriores)
    else {
        resultSentiment = "Neutro";
        confidence = 0.70;
    }

    SentimentAnalysis entity = SentimentMapper.toEntity(request, resultSentiment, confidence);
    SentimentAnalysis savedEntity = sentimentRepository.save(entity);

    return SentimentMapper.toResponse(savedEntity);
}
}