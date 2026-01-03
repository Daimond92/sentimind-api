//package com.sentimind.sentimind_api.integration;
//
//import com.sentimind.sentimind_api.model.SentimentAnalysis;
//import com.sentimind.sentimind_api.repository.SentimentRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Testcontainers
//@DisplayName("PostgreSQL Integration Tests")
//class PostgreSQLIntegrationTest {
//
//    @Container
//    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
//            .withDatabaseName("testdb")
//            .withUsername("test")
//            .withPassword("test");
//
//    @DynamicPropertySource
//    static void configureProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgres::getJdbcUrl);
//        registry.add("spring.datasource.username", postgres::getUsername);
//        registry.add("spring.datasource.password", postgres::getPassword);
//        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
//    }
//
//    @Autowired
//    private SentimentRepository sentimentRepository;
//
//    @Test
//    @DisplayName("Debe conectar correctamente a PostgreSQL")
//    void testPostgreSQLConnection() {
//        assertTrue(postgres.isRunning(), "El contenedor de PostgreSQL debe estar corriendo");
//    }
//
//    @Test
//    @DisplayName("Debe guardar y recuperar análisis de sentimiento")
//    void testSaveAndRetrieveSentiment() {
//        // Crear entidad
//        SentimentAnalysis analysis = new SentimentAnalysis();
//        analysis.setText("Este es un texto de prueba para PostgreSQL");
//        analysis.setSentiment("Positivo");
//        analysis.setConfidence(0.95);
//
//        // Guardar
//        SentimentAnalysis saved = sentimentRepository.save(analysis);
//
//        // Verificar que se guardó correctamente
//        assertNotNull(saved.getId());
//        assertNotNull(saved.getCreatedAt());
//
//        // Recuperar
//        SentimentAnalysis retrieved = sentimentRepository.findById(saved.getId()).orElse(null);
//
//        // Validar
//        assertNotNull(retrieved);
//        assertEquals("Este es un texto de prueba para PostgreSQL", retrieved.getText());
//        assertEquals("Positivo", retrieved.getSentiment());
//        assertEquals(0.95, retrieved.getConfidence());
//    }
//
//    @Test
//    @DisplayName("Debe manejar correctamente múltiples registros")
//    void testMultipleRecords() {
//        // Limpiar BD
//        sentimentRepository.deleteAll();
//
//        // Crear varios análisis
//        for (int i = 1; i <= 5; i++) {
//            SentimentAnalysis analysis = new SentimentAnalysis();
//            analysis.setText("Texto de prueba " + i);
//            analysis.setSentiment(i % 2 == 0 ? "Positivo" : "Negativo");
//            analysis.setConfidence(0.80 + (i * 0.02));
//            sentimentRepository.save(analysis);
//        }
//
//        // Verificar cantidad
//        long count = sentimentRepository.count();
//        assertEquals(5, count, "Deben existir exactamente 5 registros");
//    }
//
//    @Test
//    @DisplayName("Debe generar timestamp automáticamente")
//    void testAutoTimestamp() {
//        SentimentAnalysis analysis = new SentimentAnalysis();
//        analysis.setText("Prueba de timestamp");
//        analysis.setSentiment("Neutro");
//        analysis.setConfidence(0.70);
//
//        LocalDateTime before = LocalDateTime.now();
//        SentimentAnalysis saved = sentimentRepository.save(analysis);
//        LocalDateTime after = LocalDateTime.now();
//
//        assertNotNull(saved.getCreatedAt());
//        assertTrue(saved.getCreatedAt().isAfter(before.minusSeconds(1)));
//        assertTrue(saved.getCreatedAt().isBefore(after.plusSeconds(1)));
//    }
//}