package com.mayak.iet.infrastructure.connection;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class BackendPingService {

    private final RestTemplate restTemplate;
    private final BackendConnectionMonitor monitor;

    @Scheduled(fixedDelay = 3_000)
    public void ping() {
        try {
            restTemplate.getForEntity("/actuator/health", String.class);
            monitor.markConnected();
        } catch (Exception e) {
            monitor.markDisconnected(e);
        }
    }
}