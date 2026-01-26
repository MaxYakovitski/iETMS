package com.mayak.iet.app;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "backend")
public record BackendProperties (
        String baseUrl,
        String wsUrl) {
}