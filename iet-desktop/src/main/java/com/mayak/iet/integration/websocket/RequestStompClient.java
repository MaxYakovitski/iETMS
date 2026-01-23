package com.mayak.iet.integration.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mayak.iet.request.dto.event.RequestEventDto;
import com.mayak.iet.request.event.RequestEvent;
import com.mayak.iet.integration.auth.AuthState;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.mayak.iet.ws.WsDestinations.QUEUE_REQUESTS;
import static com.mayak.iet.ws.WsDestinations.TOPIC_REQUESTS;

@Component
@Slf4j
public class RequestStompClient {

    private static final TypeReference<RequestEvent<RequestEventDto>> EVENT_TYPE =
            new TypeReference<>() {};

    private final WebSocketStompClient stompClient;
    private final ObjectMapper mapper;
    private final String url;
    private final AuthState authState;

    private volatile StompSession session;
    private volatile boolean connected;

    private Consumer<RequestEvent<RequestEventDto>> lastTopicHandler;
    private Consumer<RequestEvent<RequestEventDto>> lastUserHandler;

    private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();

    public RequestStompClient(AuthState authState, @Value("${backend.ws-url:ws://localhost:8080/ws}") String url) {
        this.authState = authState;
        this.url = url;
        this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public void connect(
            Consumer<RequestEvent<RequestEventDto>> onTopicEvent,
            Consumer<RequestEvent<RequestEventDto>> onUserEvent) {

        this.lastTopicHandler = onTopicEvent;
        this.lastUserHandler = onUserEvent;

        if (connected) {
            log.debug("WS already connected, skipping connect()");
            return;
        }

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        if (authState.isAuthenticated()) {
            headers.setBearerAuth(authState.getToken());
            log.info("WS auth header set");
        } else {
            log.warn("WS connection without auth token");
        }

        stompClient.connectAsync(url, headers, new StompSessionHandlerAdapter() {

                    @Override
                    public void afterConnected(@NotNull StompSession session, @NotNull StompHeaders headers) {
                        RequestStompClient.this.session = session;
                        connected = true;

                        //GLOBAL EVENTS
                        session.subscribe(TOPIC_REQUESTS, frameHandler(onTopicEvent));
                        //USER-SPECIFIC EVENTS
                        session.subscribe("/user" + QUEUE_REQUESTS, frameHandler(onUserEvent));
                    }

                    @Override
                    public void handleTransportError(@NotNull StompSession session, @NotNull Throwable exception) {
                        connected = false;
                        log.warn("WS transport error", exception);

                        scheduleReconnect();
                    }
                }
        );
    }

    private void scheduleReconnect() {
        reconnectExecutor.schedule(() -> connect(lastTopicHandler, lastUserHandler), 3, TimeUnit.SECONDS);
    }

    private StompFrameHandler frameHandler(
            Consumer<RequestEvent<RequestEventDto>> consumer
    ) {
        return new StompFrameHandler() {

            @Override
            public @NotNull Type getPayloadType(@NotNull StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(@NotNull StompHeaders headers, Object payload) {
                if (!(payload instanceof byte[] bytes)) {
                    log.warn("WS unexpected payload type: {}",
                            payload == null ? "null" : payload.getClass().getName());
                    return;
                }

                try {
                    RequestEvent<RequestEventDto> event =
                            mapper.readValue(bytes, EVENT_TYPE);
                    consumer.accept(event);
                } catch (Exception e) {
                    log.error("WS payload deserialize failed", e);
                }
            }
        };
    }

    public synchronized void disconnect() {
        if (session != null && session.isConnected()) {
            try {
                session.disconnect();
                log.info("WS disconnected");
            } catch (Exception e) {
                log.warn("WS disconnect failed", e);
            }
        }
        session = null;
        connected = false;
    }
}