package com.mayak.ietms.integration.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mayak.ietms.app.BackendProperties;
import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.company.event.CompanyEvent;
import com.mayak.ietms.integration.auth.AuthState;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.mayak.ietms.ws.WsDestinations.TOPIC_COMPANIES;

@Component
@Slf4j
public class CompanyStompClient extends AbstractStompClient{

    private static final TypeReference<CompanyEvent<CompanyDto>> EVENT_TYPE =
            new TypeReference<>() {};

    private final WebSocketStompClient stompClient;
    private final ObjectMapper mapper;
    private final String wsUrl;
    private final AuthState authState;

    private final List<Consumer<CompanyEvent<CompanyDto>>> handlers = new CopyOnWriteArrayList<>();

    public CompanyStompClient(AuthState authState, BackendProperties backendProperties) {
        this.authState = authState;
        this.wsUrl = backendProperties.getWsUrl();
        this.stompClient = buildStompClient();
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public Runnable connect(Consumer<CompanyEvent<CompanyDto>> onEvent) {
        handlers.add(onEvent);
        requestConnect();
        if (!connected && !connecting && !shuttingDown) {
            doConnect();
        }

        return () -> {
            handlers.remove(onEvent);
            if (handlers.isEmpty()) requestDisconnect();
        };
    }

    private synchronized void doConnect() {
        if (connected || connecting || shuttingDown || !desiredConnected) return;
        connecting = true;

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        if (authState.isAuthenticated()) {
            headers.setBearerAuth(authState.getToken());
            log.info("WS auth header set");
        } else {
            log.warn("WS connection without auth token");
        }

        try {
            stompClient.connectAsync(wsUrl, headers, new StompSessionHandlerAdapter() {

                @Override
                public void afterConnected(@NotNull StompSession session, @NotNull StompHeaders headers) {
                    CompanyStompClient.this.session = session;
                    connected = true;
                    connecting = false;
                    session.subscribe(TOPIC_COMPANIES, frameHandler(e -> handlers.forEach(h -> h.accept(e))));
                }

                @Override
                public void handleTransportError(@NotNull StompSession session, @NotNull Throwable exception) {
                    connecting = false;
                    connected = false;
                    log.warn("WS transport error", exception);
                    if (!shuttingDown && desiredConnected) scheduleReconnect();
                }

                @Override
                public void handleException(@NotNull StompSession session, StompCommand command,
                                            @NotNull StompHeaders headers, @Nullable byte[] payload, @NotNull Throwable exception) {
                    connecting = false;
                    connected = false;
                    log.warn("WS STOMP exception", exception);
                    if (!shuttingDown && desiredConnected) scheduleReconnect();
                }
            });
        } catch (Exception e) {
            connecting = false;
            log.warn("WS connectAsync failed", e);
            if (!shuttingDown && desiredConnected) scheduleReconnect();
        }

    }

    private void scheduleReconnect() {
        if (!desiredConnected || shuttingDown) return;
        reconnectExecutor.schedule(this::doConnect, 3, TimeUnit.SECONDS);
    }

    private StompFrameHandler frameHandler(Consumer<CompanyEvent<CompanyDto>> consumer) {
        return new StompFrameHandler() {

            @Override
            public @NotNull Type getPayloadType(@NotNull StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(@NotNull StompHeaders headers, Object payload) {
                if (!(payload instanceof byte[] bytes)) return;
                try {
                    CompanyEvent<CompanyDto> event = mapper.readValue(bytes, EVENT_TYPE);
                    consumer.accept(event);
                } catch (Exception e) {
                    log.error("Company WS deserialize failed", e);
                }
            }
        };
    }

}