package com.sentimind.sentimind_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentimind.sentimind_api.dto.SentimentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Sentiment API - Integration Tests")
class SentimentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private SentimentRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new SentimentRequest("Este producto es absolutamente excelente y maravilloso");
    }

    @Test
    @DisplayName("POST /api/v1/sentiment - Debe analizar sentimiento positivo")
    void testAnalyzeSentimentPositive() throws Exception {
        mockMvc.perform(post("/api/v1/sentiment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sentiment").value("Positivo"))
                .andExpect(jsonPath("$.confidence").isNumber())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("POST /api/v1/sentiment - Debe rechazar texto vacío")
    void testAnalyzeSentimentEmptyText() throws Exception {
        SentimentRequest emptyRequest = new SentimentRequest("");

        mockMvc.perform(post("/api/v1/sentiment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @DisplayName("POST /api/v1/sentiment - Debe rechazar texto muy corto")
    void testAnalyzeSentimentTooShort() throws Exception {
        SentimentRequest shortRequest = new SentimentRequest("Hola");

        mockMvc.perform(post("/api/v1/sentiment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/sentiment - Debe rechazar texto muy largo")
    void testAnalyzeSentimentTooLong() throws Exception {
        SentimentRequest longRequest = new SentimentRequest("a".repeat(501));

        mockMvc.perform(post("/api/v1/sentiment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(longRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /health - Debe verificar salud de la API")
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /swagger-ui.html - Debe estar accesible")
    void testSwaggerUIAccessible() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("GET /v3/api-docs - Debe devolver documentación OpenAPI")
    void testOpenAPIDocsAccessible() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").exists());
    }


}