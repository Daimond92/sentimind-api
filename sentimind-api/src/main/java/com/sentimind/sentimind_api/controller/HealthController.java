package com.sentimind.sentimind_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.time.LocalDateTime;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        return Map.of(
            "status", "UP",
            "message", "Sentimind API está funcionando correctamente",
            "timestamp", LocalDateTime.now()
        );
    }
}