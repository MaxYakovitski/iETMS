package com.mayak.ietms.infrastructure.connection;

import com.mayak.ietms.ui.core.SessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class BackendPingService {

    private final RestTemplate restTemplate;
    private final BackendConnectionMonitor monitor;
    private final SessionManager sessionManager;

    @Scheduled(fixedDelay = 3_000)
    public void ping() {
        try {
            restTemplate.getForEntity("/actuator/health", String.class);
            monitor.markConnected();
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().value() == 401) {
                sessionManager.handleSessionExpired();
                return;
            }
            monitor.markDisconnected(e);
        } catch (Exception e) {
            monitor.markDisconnected(e);
        }
    }
}