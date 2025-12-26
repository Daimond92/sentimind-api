package com.sentimind.sentimind_api.exception;

import java.util.Map;

public record ErrorResponse(String error) {
    public static ErrorResponse of(String error) {
        return new ErrorResponse(error);
    }
    
    public Map<String, String> toMap() {
        return Map.of("error", error);
    }
}
