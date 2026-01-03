package com.sentimind.sentimind_api.repository;

import com.sentimind.sentimind_api.model.SentimentAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SentimentRepository extends JpaRepository <SentimentAnalysis, Long> {
}
