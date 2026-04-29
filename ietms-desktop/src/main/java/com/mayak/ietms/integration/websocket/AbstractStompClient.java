package com.mayak.ietms.integration.websocket;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

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
     * Creates a pre-configured {@link WebSocketStompClient} shared by all
     * subclasses. Heartbeat is set to 25 s in both directions to keep the
     * underlying TCP connection alive through NAT idle-timeout eviction.
     * A dedicated {@link ThreadPoolTaskScheduler} is required by
     * {@link WebSocketStompClient} to dispatch heartbeat frames and must be
     * explicitly initialized before use.
     */
    protected static WebSocketStompClient buildStompClient() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.initialize();

        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new MappingJackson2MessageConverter());
        client.setTaskScheduler(scheduler);
        client.setDefaultHeartbeat(new long[]{25_000, 25_000});
        return client;
    }

    /**
     * Request WS to stay connected for the lifetime of the session.
     * Idempotent.
     */
    public synchronized void requestConnect() {
        desiredConnected = true;
        log.debug("{} requestConnect()", getClass().getSimpleName());
    }

    /**
     * Request WS to disconnect and stay disconnected.
     * Used on logout / app shutdown.
     */
    public synchronized void requestDisconnect() {
        desiredConnected = false;
        log.debug("{} requestDisconnect()", getClass().getSimpleName());
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