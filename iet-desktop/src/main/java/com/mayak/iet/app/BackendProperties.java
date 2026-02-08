package com.mayak.iet.app;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class BackendProperties {

    private final String baseUrl;
    private final String wsUrl;

    public BackendProperties(
            @Value("${backend.base-url}") String baseUrl,
            @Value("${backend.ws-url}") String wsUrl) {
        this.baseUrl = baseUrl;
        this.wsUrl = wsUrl;
    }

}