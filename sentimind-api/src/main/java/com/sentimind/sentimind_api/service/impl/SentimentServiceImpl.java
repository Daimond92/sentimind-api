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
        // 1. Lógica de "IA Simulada" (Mock Intelligence)
        String text = request.text().toLowerCase();
        String resultSentiment = "Negativo";
        double confidence = 0.75;

        if (text.contains("bueno") || text.contains("excelente") || text.contains("increíble")) {
            resultSentiment = "Positivo";
            confidence = 0.95;
        }

        // 2. Uso del Mapper para crear la Entidad
        SentimentAnalysis entity = SentimentMapper.toEntity(request, resultSentiment, confidence);

        // 3. Guardar en DB
        SentimentAnalysis savedEntity = sentimentRepository.save(entity);

        // 4. Uso del Mapper para devolver la Respuesta
        return SentimentMapper.toResponse(savedEntity);
    }
}