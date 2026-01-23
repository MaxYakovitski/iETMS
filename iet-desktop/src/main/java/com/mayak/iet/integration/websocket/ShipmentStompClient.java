package com.mayak.iet.integration.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mayak.iet.request.dto.event.ShipmentEventDto;
import com.mayak.iet.shipment.event.ShipmentEvent;
import com.mayak.iet.integration.auth.AuthState;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.mayak.iet.ws.WsDestinations.QUEUE_SHIPMENTS;
import static com.mayak.iet.ws.WsDestinations.TOPIC_SHIPMENTS;

@Component
@Slf4j
public class ShipmentStompClient {

    private static final TypeReference<ShipmentEvent<ShipmentEventDto>> EVENT_TYPE =
            new TypeReference<>() {};

    private final WebSocketStompClient stompClient;
    private final ObjectMapper mapper;
    private final String url;
    private final AuthState authState;

    private volatile StompSession session;
    private volatile boolean connected;

    private Consumer<ShipmentEvent<ShipmentEventDto>> lastTopicHandler;
    private Consumer<ShipmentEvent<ShipmentEventDto>> lastUserHandler;

    private final ScheduledExecutorService reconnectExecutor =
            Executors.newSingleThreadScheduledExecutor();

    public ShipmentStompClient(AuthState authState, @Value("${backend.ws-url:ws://localhost:8080/ws}") String url
    ) {
        this.authState = authState;
        this.url = url;
        this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public void connect(
            Consumer<ShipmentEvent<ShipmentEventDto>> onTopicEvent,
            Consumer<ShipmentEvent<ShipmentEventDto>> onUserEvent
    ) {
        this.lastTopicHandler = onTopicEvent;
        this.lastUserHandler = onUserEvent;

        if (connected) {
            log.debug("Shipment WS already connected");
            return;
        }

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        if (authState.isAuthenticated()) {
            headers.setBearerAuth(authState.getToken());
        }

        stompClient.connectAsync(url, headers, new StompSessionHandlerAdapter() {

            @Override
            public void afterConnected(@NotNull StompSession session, @NotNull StompHeaders headers) {
                ShipmentStompClient.this.session = session;
                connected = true;

                session.subscribe(TOPIC_SHIPMENTS, frameHandler(onTopicEvent));
                session.subscribe("/user" + QUEUE_SHIPMENTS, frameHandler(onUserEvent));
            }

            @Override
            public void handleTransportError(@NotNull StompSession session, @NotNull Throwable exception) {
                connected = false;
                log.warn("Shipment WS transport error", exception);
                scheduleReconnect();
            }
        });
    }

    private void scheduleReconnect() {
        reconnectExecutor.schedule(() -> connect(lastTopicHandler, lastUserHandler), 3, TimeUnit.SECONDS);
    }

    private StompFrameHandler frameHandler(
            Consumer<ShipmentEvent<ShipmentEventDto>> consumer
    ) {
        return new StompFrameHandler() {

            @Override
            public @NotNull Type getPayloadType(@NotNull StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(@NotNull StompHeaders headers, Object payload) {
                if (!(payload instanceof byte[] bytes)) {
                    return;
                }
                try {
                    ShipmentEvent<ShipmentEventDto> event =
                            mapper.readValue(bytes, EVENT_TYPE);
                    consumer.accept(event);
                } catch (Exception e) {
                    log.error("Shipment WS deserialize failed", e);
                }
            }
        };
    }

    public synchronized void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        session = null;
        connected = false;
    }
}
