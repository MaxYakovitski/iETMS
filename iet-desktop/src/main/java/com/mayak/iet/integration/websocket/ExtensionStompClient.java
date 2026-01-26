package com.mayak.iet.integration.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mayak.iet.app.BackendProperties;
import com.mayak.iet.extension.event.ExtensionDraftInvalidEvent;
import com.mayak.iet.integration.auth.AuthState;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
@Slf4j
public class ExtensionStompClient extends AbstractStompClient {

    private static final TypeReference<ExtensionDraftInvalidEvent> EVENT_TYPE =
            new TypeReference<>() {};

    private final WebSocketStompClient stompClient;
    private final ObjectMapper mapper;
    private final String wsUrl;
    private final AuthState authState;

    private Consumer<ExtensionDraftInvalidEvent> lastHandler;

    public ExtensionStompClient(AuthState authState, BackendProperties backendProperties) {
        this.authState = authState;
        this.wsUrl = backendProperties.wsUrl();
        this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public synchronized void connect(Consumer<ExtensionDraftInvalidEvent> onEvent) {
        this.lastHandler = onEvent;
        if (connected || shuttingDown) {
            log.debug("Extension WS already connected");
            return;
        }

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        if (authState.isAuthenticated()) {
            headers.setBearerAuth(authState.getToken());
        }

        stompClient.connectAsync(wsUrl, headers, new StompSessionHandlerAdapter() {

            @Override
            public void afterConnected(@NotNull StompSession session, @NotNull StompHeaders headers) {
                ExtensionStompClient.this.session = session;
                connected = true;
                session.subscribe("/user/queue/extension", frameHandler(onEvent));
                log.info("WS EXTENSION connected");
            }

            @Override
            public void handleTransportError(
                    @NotNull StompSession session,
                    @NotNull Throwable exception
            ) {
                connected = false;
                log.warn("Extension WS transport error", exception);
                if (!shuttingDown) scheduleReconnect();
            }
        });
    }

    private StompFrameHandler frameHandler(Consumer<ExtensionDraftInvalidEvent> consumer) {
        return new StompFrameHandler() {

            @Override
            public @NotNull Type getPayloadType(@NotNull StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(@NotNull StompHeaders headers, Object payload) {

                if (!(payload instanceof byte[] bytes)) {
                    log.warn("Extension WS unexpected payload: {}",
                            payload == null ? "null" : payload.getClass().getName());
                    return;
                }

                try {
                    ExtensionDraftInvalidEvent event =
                            mapper.readValue(bytes, EVENT_TYPE);
                    consumer.accept(event);
                } catch (Exception e) {
                    log.error("Extension WS payload deserialize failed", e);
                }
            }
        };
    }

    private void scheduleReconnect() {
        if (lastHandler == null) return;
        reconnectExecutor.schedule(() -> connect(lastHandler), 3, TimeUnit.SECONDS);
    }

    public synchronized void disconnect() {
        disconnectInternal("Extension");
    }
}