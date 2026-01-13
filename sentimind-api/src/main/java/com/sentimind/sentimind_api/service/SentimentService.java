package com.sentimind.sentimind_api.service;

import com.sentimind.sentimind_api.dto.SentimentRequest;
import com.sentimind.sentimind_api.dto.SentimentResponse;

import java.util.List;

public interface SentimentService {
    SentimentResponse analyzeSentiment(SentimentRequest request);
    List<SentimentResponse> getAllAnalysis();
}