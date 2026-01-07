-- Schema Sentimind - Sentiment Analysis

CREATE TABLE IF NOT EXISTS sentiment_analysis (
    id BIGSERIAL PRIMARY KEY,
    text VARCHAR(500) NOT NULL,
    sentiment VARCHAR(50) NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);