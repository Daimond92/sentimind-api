package com.sentimind.sentimind_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "sentiment_analysis")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter

public class SentimentAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String text;

    @Column(nullable = false)
    private String sentiment;

    @Column(nullable = false)
    private Double confidence;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
