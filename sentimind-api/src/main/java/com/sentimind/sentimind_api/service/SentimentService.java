package com.sentimind.sentimind_api.service;

import com.sentimind.sentimind_api.dto.SentimentRequest;
import com.sentimind.sentimind_api.dto.SentimentResponse;

public interface SentimentService {
    SentimentResponse analyzeSentiment(SentimentRequest request);
}