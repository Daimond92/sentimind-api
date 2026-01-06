package com.sentimind.sentimind_api.client;

import com.sentimind.sentimind_api.dto.SentimentRequest;
import com.sentimind.sentimind_api.dto.AiModelResponse; // Importamos el nuevo DTO
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class SentimentClient {

    private final WebClient webClient;

    @Value("${ai.api.url:http://localhost:5000/predict}")
    private String aiUrl;

    public SentimentClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public AiModelResponse getAiPrediction(String text) {
        try {
            return webClient.post()
                    .uri(aiUrl)
                    .bodyValue(new SentimentRequest(text))
                    .retrieve()
                    .bodyToMono(AiModelResponse.class)
                    .block();
        } catch (Exception e) {
            return new AiModelResponse("Error", 0.0);
        }
    }
}