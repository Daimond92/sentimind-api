package com.sentimind.sentimind_api.controller;

import com.sentimind.sentimind_api.dto.SentimentRequest;
import com.sentimind.sentimind_api.dto.SentimentResponse;
import com.sentimind.sentimind_api.service.SentimentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sentiment")
@Tag(name = "Sentiment Analysis", description = "Endpoints para el procesamiento y análisis de reseñas")
@CrossOrigin(origins = "*") // Permite peticiones desde el Frontend si es necesario
public class SentimentController {

    private final SentimentService sentimentService;

    // Inyección por constructor: La mejor práctica recomendada por Spring
    public SentimentController(SentimentService sentimentService) {
        this.sentimentService = sentimentService;
    }

    @PostMapping
    @Operation(summary = "Analiza el sentimiento de un texto y lo guarda en la base de datos")
    public ResponseEntity<SentimentResponse> analyze(@Valid @RequestBody SentimentRequest request) {
        // Llama a tu lógica en el Service (David)
        SentimentResponse response = sentimentService.analyzeSentiment(request);
        
        // Retornamos un 201 Created o 200 OK
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
