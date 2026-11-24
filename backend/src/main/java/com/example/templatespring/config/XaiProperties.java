package com.example.templatespring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "xai")
public record XaiProperties(
        boolean enabled,
        String apiKey,
        String baseUrl,
        String model,
        Long intervaloMinimoMs,
        Integer maxCompletionTokens) {}
