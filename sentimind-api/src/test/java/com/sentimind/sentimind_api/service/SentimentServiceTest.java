package com.sentimind.sentimind_api.service;

import com.sentimind.sentimind_api.client.SentimentClient;
import com.sentimind.sentimind_api.dto.SentimentRequest;
import com.sentimind.sentimind_api.dto.SentimentResponse;
import com.sentimind.sentimind_api.mapper.SentimentMapper;
import com.sentimind.sentimind_api.model.SentimentAnalysis;
import com.sentimind.sentimind_api.repository.SentimentRepository;
import com.sentimind.sentimind_api.service.impl.SentimentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Sentiment Service - Unit Tests")
class SentimentServiceTest {

    @Mock
    private SentimentRepository repository;

    @Mock
    private SentimentMapper mapper;

    @Mock
    private SentimentClient aiClient;

    private SentimentServiceImpl sentimentService;

    private SentimentRequest positiveRequest;
    private SentimentRequest negativeRequest;
    private SentimentRequest neutralRequest;

    @BeforeEach
    void setUp() {
                sentimentService = new SentimentServiceImpl(
                aiClient,      // 1. SentimentClient
                repository,    // 2. SentimentRepository
                mapper         // 3. SentimentMapper
        );
        // nota: isAiEnabled se configura con @Value desde properties
        // En tests, por defecto será false (usa lógica mock)

        positiveRequest = new SentimentRequest("Este producto es excelente y maravillosa calidad");
        negativeRequest = new SentimentRequest("Horrible experiencia, terrible y malo servicio pésimo");
        neutralRequest = new SentimentRequest("El producto llegó en tiempo y forma según lo esperado");
    }

    @Test
    @DisplayName("Debe analizar texto positivo correctamente")
    void testAnalyzePositiveSentiment() {
        // Arrange
        SentimentAnalysis mockEntity = new SentimentAnalysis();
        mockEntity.setId(1L);
        mockEntity.setText(positiveRequest.text());
        mockEntity.setSentiment("Positivo");
        mockEntity.setConfidence(0.95);
        mockEntity.setCreatedAt(LocalDateTime.now());

        SentimentResponse mockResponse = new SentimentResponse(
                1L,
                "Positivo",
                0.95,
                LocalDateTime.now(),
                positiveRequest.text()
        );

        when(mapper.toEntity(any(SentimentRequest.class), anyString(), anyDouble()))
                .thenReturn(mockEntity);
        when(repository.save(any(SentimentAnalysis.class)))
                .thenReturn(mockEntity);
        when(mapper.toResponse(any(SentimentAnalysis.class)))
                .thenReturn(mockResponse);

        // Act
        SentimentResponse response = sentimentService.analyzeSentiment(positiveRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Positivo", response.sentiment());
        assertEquals(0.95, response.confidence());
        verify(repository, times(1)).save(any(SentimentAnalysis.class));
    }

    @Test
    @DisplayName("Debe analizar texto negativo correctamente")
    void testAnalyzeNegativeSentiment() {
        // Arrange
        SentimentAnalysis mockEntity = new SentimentAnalysis();
        mockEntity.setId(2L);
        mockEntity.setText(negativeRequest.text());
        mockEntity.setSentiment("Negativo");
        mockEntity.setConfidence(0.90);
        mockEntity.setCreatedAt(LocalDateTime.now());

        SentimentResponse mockResponse = new SentimentResponse(
                2L,
                "Negativo",
                0.90,
                LocalDateTime.now(),
                negativeRequest.text()
        );

        when(mapper.toEntity(any(SentimentRequest.class), anyString(), anyDouble()))
                .thenReturn(mockEntity);
        when(repository.save(any(SentimentAnalysis.class)))
                .thenReturn(mockEntity);
        when(mapper.toResponse(any(SentimentAnalysis.class)))
                .thenReturn(mockResponse);

        // Act
        SentimentResponse response = sentimentService.analyzeSentiment(negativeRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Negativo", response.sentiment());
        assertEquals(0.90, response.confidence());
        verify(repository, times(1)).save(any(SentimentAnalysis.class));
    }

    @Test
    @DisplayName("Debe analizar texto neutro correctamente")
    void testAnalyzeNeutralSentiment() {
        // Arrange
        SentimentAnalysis mockEntity = new SentimentAnalysis();
        mockEntity.setId(3L);
        mockEntity.setText(neutralRequest.text());
        mockEntity.setSentiment("Neutro");
        mockEntity.setConfidence(0.70);
        mockEntity.setCreatedAt(LocalDateTime.now());

        SentimentResponse mockResponse = new SentimentResponse(
                3L,
                "Neutro",
                0.70,
                LocalDateTime.now(),
                neutralRequest.text()
        );

        when(mapper.toEntity(any(SentimentRequest.class), anyString(), anyDouble()))
                .thenReturn(mockEntity);
        when(repository.save(any(SentimentAnalysis.class)))
                .thenReturn(mockEntity);
        when(mapper.toResponse(any(SentimentAnalysis.class)))
                .thenReturn(mockResponse);

        // Act
        SentimentResponse response = sentimentService.analyzeSentiment(neutralRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Neutro", response.sentiment());
        assertEquals(0.70, response.confidence());
        verify(repository, times(1)).save(any(SentimentAnalysis.class));
    }

    @Test
    @DisplayName("Debe manejar texto con caracteres especiales")
    void testSpecialCharacters() {
        // Arrange
        SentimentRequest specialRequest = new SentimentRequest("¡Excelente! Muy bueno :) #happy 😊");

        SentimentAnalysis mockEntity = new SentimentAnalysis();
        mockEntity.setId(4L);
        mockEntity.setText(specialRequest.text());
        mockEntity.setSentiment("Positivo");
        mockEntity.setConfidence(0.95);
        mockEntity.setCreatedAt(LocalDateTime.now());

        SentimentResponse mockResponse = new SentimentResponse(
                4L,
                "Positivo",
                0.95,
                LocalDateTime.now(),
                specialRequest.text()
        );

        when(mapper.toEntity(any(SentimentRequest.class), anyString(), anyDouble()))
                .thenReturn(mockEntity);
        when(repository.save(any(SentimentAnalysis.class)))
                .thenReturn(mockEntity);
        when(mapper.toResponse(any(SentimentAnalysis.class)))
                .thenReturn(mockResponse);

        // Act
        SentimentResponse response = sentimentService.analyzeSentiment(specialRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Positivo", response.sentiment());
        verify(repository, times(1)).save(any(SentimentAnalysis.class));
    }

    @Test
    @DisplayName("Debe ser case-insensitive")
    void testCaseInsensitive() {
        // Arrange
        SentimentRequest upperRequest = new SentimentRequest("EXCELENTE PRODUCTO, MUY BUENO");

        SentimentAnalysis mockEntity = new SentimentAnalysis();
        mockEntity.setId(5L);
        mockEntity.setText(upperRequest.text());
        mockEntity.setSentiment("Positivo");
        mockEntity.setConfidence(0.95);
        mockEntity.setCreatedAt(LocalDateTime.now());

        SentimentResponse mockResponse = new SentimentResponse(
                5L,
                "Positivo",
                0.95,
                LocalDateTime.now(),
                upperRequest.text()
        );

        when(mapper.toEntity(any(SentimentRequest.class), anyString(), anyDouble()))
                .thenReturn(mockEntity);
        when(repository.save(any(SentimentAnalysis.class)))
                .thenReturn(mockEntity);
        when(mapper.toResponse(any(SentimentAnalysis.class)))
                .thenReturn(mockResponse);

        // Act
        SentimentResponse response = sentimentService.analyzeSentiment(upperRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Positivo", response.sentiment());
        verify(repository, times(1)).save(any(SentimentAnalysis.class));
    }
}