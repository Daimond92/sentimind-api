package com.sentimind.sentimind_api.service;

import com.sentimind.sentimind_api.dto.*;
import com.sentimind.sentimind_api.model.SentimentAnalysis;
import com.sentimind.sentimind_api.repository.SentimentRepository;
import com.sentimind.sentimind_api.service.impl.SentimentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Sentiment Service - Unit Tests")
class SentimentServiceTest {

    @Mock
    private SentimentRepository repository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    @SuppressWarnings("rawtypes") 
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private SentimentServiceImpl sentimentService;

    @BeforeEach
    @SuppressWarnings("unchecked") // Soluciona: "Type safety: unchecked conversion"
    void setUp() {
        lenient().when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.build()).thenReturn(webClient);
        
        lenient().when(webClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        sentimentService = new SentimentServiceImpl(webClientBuilder, repository);
    }

    @Test
    @DisplayName("Debe analizar y guardar un sentimiento correctamente")
    void testAnalyzeSentimentSuccess() {
        // Arrange
        AiModelResponse mockAiResponse = new AiModelResponse("positivo", 0.98);
        when(responseSpec.bodyToMono(AiModelResponse.class)).thenReturn(Mono.just(mockAiResponse));

        SentimentAnalysis mockEntity = new SentimentAnalysis();
        mockEntity.setId(1L);
        mockEntity.setText("Excelente servicio");
        mockEntity.setSentiment("positivo");
        mockEntity.setConfidence(0.98);
        mockEntity.setCreatedAt(LocalDateTime.now());

        when(repository.save(any(SentimentAnalysis.class))).thenReturn(mockEntity);

        // Act
        SentimentResponse response = sentimentService.analyzeSentiment(new SentimentRequest("Excelente servicio"));

        // Assert
        assertNotNull(response);
        assertEquals("positivo", response.sentiment());
        verify(repository, times(1)).save(any(SentimentAnalysis.class));
    }
}