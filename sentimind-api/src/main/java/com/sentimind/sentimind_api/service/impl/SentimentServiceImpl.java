package com.sentimind.sentimind_api.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.sentimind.sentimind_api.dto.*;
import com.sentimind.sentimind_api.model.SentimentAnalysis;
import com.sentimind.sentimind_api.repository.SentimentRepository;
import com.sentimind.sentimind_api.service.SentimentService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SentimentServiceImpl implements SentimentService {

    private final WebClient webClient;
    private final SentimentRepository sentimentRepository;

    public SentimentServiceImpl(WebClient.Builder webClientBuilder, SentimentRepository sentimentRepository) {
        // Conexión con la API de Python
        this.webClient = webClientBuilder.baseUrl("http://sentimind-ai:8000").build();
        this.sentimentRepository = sentimentRepository;
    }

    @Override
public SentimentResponse analyzeSentiment(SentimentRequest request) {
    // 1. Llamada a la API de Python
    AiModelResponse aiResponse = this.webClient.post()
            .uri("/predict")
            .bodyValue(Map.of("text", request.text()))
            .retrieve()
            .bodyToMono(AiModelResponse.class)
            .block();

    // 2. Uso del modelo SentimentAnalysis.java
    SentimentAnalysis analysis = new SentimentAnalysis();
    analysis.setText(request.text()); 
    
    // USANDO LOS CAMPOS EXACTOS DE TU AiModelResponse
    analysis.setSentiment(aiResponse.prevision());   // Antes era aiResponse.sentiment()
    analysis.setConfidence(aiResponse.probabilidad()); // Antes era aiResponse.confidence()

    // 3. Guardado en la base de datos
    SentimentAnalysis saved = sentimentRepository.save(analysis);

    // 4. Retorno del DTO final
    return new SentimentResponse(
            saved.getId(),         
            saved.getSentiment(),  
            saved.getConfidence(), 
            saved.getCreatedAt(),  // Se mapea al campo 'timestamp' de tu record
            saved.getText()        
    );
}

    @Override
    public List<SentimentResponse> getAllAnalysis() {
        return sentimentRepository.findAll().stream()
                .map(saved -> new SentimentResponse(
                        saved.getId(),
                        saved.getSentiment(),
                        saved.getConfidence(),
                        saved.getCreatedAt(),
                        saved.getText()
                ))
                .collect(Collectors.toList());
    }
}