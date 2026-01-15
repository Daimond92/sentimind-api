package com.sentimind.sentimind_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiModelResponse(

    String prevision,
    Double probabilidad
) {}