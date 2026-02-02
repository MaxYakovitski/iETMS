package com.mayak.iet.integration.websocket;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class AbstractStompClient {

    protected volatile StompSession session;
    protected volatile boolean connected;
    protected volatile boolean shuttingDown;

    protected volatile boolean desiredConnected;

    protected final ScheduledExecutorService reconnectExecutor =
            Executors.newSingleThreadScheduledExecutor();

    /**
     * Mark this client as "should stay connected" and connect if not connected.
     * Idempotent.
     */
    public synchronized void requestConnect() {
        desiredConnected = true;
    }

    /**
     * Mark this client as "should NOT stay connected".
     * Used on logout / app shutdown.
     */
    public synchronized void requestDisconnect() {
        desiredConnected = false;
        disconnectSession(getClass().getSimpleName());
    }

    protected synchronized void disconnectSession(String name) {

        if (session != null && session.isConnected()) {
            try {
                session.disconnect();
                log.info("{} WS disconnected", name);
            } catch (Exception e) {
                log.warn("{} WS disconnect failed", name, e);
            }
        }

        session = null;
        connected = false;
    }

    @PreDestroy
    public void shutdown() {
        shuttingDown = true;
        desiredConnected = false;
        reconnectExecutor.shutdownNow();
        log.info("{} WS executor shutdown", getClass().getSimpleName());
    }
}
