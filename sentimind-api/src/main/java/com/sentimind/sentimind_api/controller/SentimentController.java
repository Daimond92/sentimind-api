package com.sentimind.sentimind_api.controller;

import com.sentimind.sentimind_api.dto.SentimentRequest;
import com.sentimind.sentimind_api.dto.SentimentResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/sentiment")
public class SentimentController {

    @PostMapping
    public ResponseEntity<SentimentResponse> analyzeSentiment(@Valid @RequestBody SentimentRequest request) {
        // Aquí iría la lógica de análisis de sentimiento
        // Por ahora, devolvemos una respuesta de ejemplo
        SentimentResponse response = new SentimentResponse(
                1L,
                "POSITIVO",
                0.95,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }
}
