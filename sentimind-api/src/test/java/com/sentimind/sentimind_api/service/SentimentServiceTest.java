package com.sentimind.sentimind_api.service;

import com.sentimind.sentimind_api.dto.SentimentRequest;
import com.sentimind.sentimind_api.dto.SentimentResponse;
import com.sentimind.sentimind_api.model.SentimentAnalysis;
import com.sentimind.sentimind_api.repository.SentimentRepository;
import com.sentimind.sentimind_api.service.impl.SentimentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Sentiment Service - Unit Tests")
class SentimentServiceTest {

    @Mock
    private SentimentRepository sentimentRepository;

    @InjectMocks
    private SentimentServiceImpl sentimentService;

    private SentimentRequest positiveRequest;
    private SentimentRequest negativeRequest;
    private SentimentRequest neutralRequest;

    @BeforeEach
    void setUp() {
        positiveRequest = new SentimentRequest("Este producto es excelente y maravillosa calidad");
        negativeRequest = new SentimentRequest("Horrible experiencia, terrible y malo servicio pésimo");
        neutralRequest = new SentimentRequest("El producto llegó en tiempo y forma según lo esperado");
    }

    @Test
    @DisplayName("Debe analizar texto positivo correctamente")
    void testAnalyzePositiveSentiment() {

        SentimentAnalysis mockEntity = new SentimentAnalysis();
        mockEntity.setId(1L);
        mockEntity.setText(positiveRequest.text());
        mockEntity.setSentiment("Positivo");
        mockEntity.setConfidence(0.95);

        when(sentimentRepository.save(any(SentimentAnalysis.class))).thenReturn(mockEntity);

        SentimentResponse response = sentimentService.analyzeSentiment(positiveRequest);

        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals("Positivo", response.sentiment(), "Debe detectar sentimiento positivo");
        assertEquals(0.95, response.confidence(), "La confianza debe ser 0.95");
        verify(sentimentRepository, times(1)).save(any(SentimentAnalysis.class));
    }

    @Test
    @DisplayName("Debe analizar texto negativo correctamente")
    void testAnalyzeNegativeSentiment() {

        SentimentAnalysis mockEntity = new SentimentAnalysis();
        mockEntity.setId(2L);
        mockEntity.setText(negativeRequest.text());
        mockEntity.setSentiment("Negativo");
        mockEntity.setConfidence(0.90);

        when(sentimentRepository.save(any(SentimentAnalysis.class))).thenReturn(mockEntity);

        SentimentResponse response = sentimentService.analyzeSentiment(negativeRequest);

        assertNotNull(response);
        assertEquals("Negativo", response.sentiment(), "Debe detectar sentimiento negativo");
        assertEquals(0.90, response.confidence());
        verify(sentimentRepository, times(1)).save(any(SentimentAnalysis.class));
    }

    @Test
    @DisplayName("Debe analizar texto neutro correctamente")
    void testAnalyzeNeutralSentiment() {

        SentimentAnalysis mockEntity = new SentimentAnalysis();
        mockEntity.setId(3L);
        mockEntity.setText(neutralRequest.text());
        mockEntity.setSentiment("Neutro");
        mockEntity.setConfidence(0.70);

        when(sentimentRepository.save(any(SentimentAnalysis.class))).thenReturn(mockEntity);

        SentimentResponse response = sentimentService.analyzeSentiment(neutralRequest);

        assertNotNull(response);
        assertEquals("Neutro", response.sentiment(), "Debe detectar sentimiento neutro");
        assertEquals(0.70, response.confidence());
        verify(sentimentRepository, times(1)).save(any(SentimentAnalysis.class));
    }
}