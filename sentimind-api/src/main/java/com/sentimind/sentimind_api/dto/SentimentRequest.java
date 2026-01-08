package com.sentimind.sentimind_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SentimentRequest(
        @NotBlank(message = "Por favor ingresa el texto de la reseña") @Size(min = 10, max = 500,
                message = "Por favor ingresa una reseña que contenga entre 10 y 500 caracteres") String text
) {}