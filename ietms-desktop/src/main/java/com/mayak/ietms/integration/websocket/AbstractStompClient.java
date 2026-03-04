package com.mayak.ietms.integration.websocket;

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
        Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName(getClass().getSimpleName() + "-reconnect");
        return t;
    });

    /**
     * Request WS to stay connected for the lifetime of the session.
     * Idempotent.
     */
    public synchronized void requestConnect() {
        desiredConnected = true;
        log.warn("{} requestConnect()", getClass().getSimpleName());
    }

    /**
     * Request WS to disconnect and stay disconnected.
     * Used on logout / app shutdown.
     */
    public synchronized void requestDisconnect() {
        desiredConnected = false;
        log.warn("{} requestDisconnect()", getClass().getSimpleName());
        disconnectSession();
    }

    protected synchronized void disconnectSession() {
        if (session != null && session.isConnected()) {
            try {
                session.disconnect();
                log.info("{} WS disconnected", getClass().getSimpleName());
            } catch (Exception e) {
                log.warn("{} WS disconnect failed", getClass().getSimpleName(), e);
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
        disconnectSession();
        log.info("{} WS executor shutdown", getClass().getSimpleName());
    }
}
