package com.mayak.iet.infrastructure.connection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class BackendPingService {

    private final RestTemplate restTemplate;
    private final BackendConnectionMonitor monitor;

    public boolean ping() {
        try {
            restTemplate.getForEntity("/actuator/health", String.class);
            monitor.markConnected();
            return true;
        } catch (Exception e) {
            monitor.markDisconnected(e);
            return false;
        }
    }
}