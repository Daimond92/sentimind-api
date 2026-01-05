package com.sentimind.sentimind_api.client;

import com.sentimind.sentimind_api.dto.SentimentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Component
public class SentimentClient {

    private final WebClient webClient;

    @Value("${ai.api.url:http://localhost:5000/predict}") // URL por defecto para el equipo de Data Science
    private String aiUrl;

    public SentimentClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Map<String, Object> getAiPrediction(String text) {
        try {
            return webClient.post()
                    .uri(aiUrl)
                    .bodyValue(new SentimentRequest(text))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(); // .block() para simplificar la integración en este MVP
        } catch (Exception e) {
            // Si la IA falla, retornamos un error controlado
            return Map.of("sentiment", "Error", "confidence", 0.0);
        }
    }
}