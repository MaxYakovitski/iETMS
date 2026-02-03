package com.mayak.iet.integration.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mayak.iet.app.BackendProperties;
import com.mayak.iet.request.dto.event.ShipmentEventDto;
import com.mayak.iet.shipment.event.ShipmentEvent;
import com.mayak.iet.integration.auth.AuthState;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.mayak.iet.ws.WsDestinations.QUEUE_SHIPMENTS;
import static com.mayak.iet.ws.WsDestinations.TOPIC_SHIPMENTS;

@Component
@Slf4j
public class ShipmentStompClient extends AbstractStompClient {

    private static final TypeReference<ShipmentEvent<ShipmentEventDto>> EVENT_TYPE =
            new TypeReference<>() {};

    private final WebSocketStompClient stompClient;
    private final ObjectMapper mapper;
    private final String wsUrl;
    private final AuthState authState;

    private Consumer<ShipmentEvent<ShipmentEventDto>> lastTopicHandler;
    private Consumer<ShipmentEvent<ShipmentEventDto>> lastUserHandler;

    public ShipmentStompClient(AuthState authState, BackendProperties backendProperties) {
        this.authState = authState;
        this.wsUrl = backendProperties.wsUrl();
        this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    /**
     * Public API.
     * Declares intent that WS must stay connected.
     */
    public void connect(
            Consumer<ShipmentEvent<ShipmentEventDto>> onTopicEvent,
            Consumer<ShipmentEvent<ShipmentEventDto>> onUserEvent
    ) {
        this.lastTopicHandler = onTopicEvent;
        this.lastUserHandler = onUserEvent;

        requestConnect();

        if (!connected && !shuttingDown) {
            doConnect();
        }
    }

    /**
     * Internal connect implementation.
     * MUST be called only if desiredConnected == true
     */
    private synchronized  void doConnect() {
        if (connected || shuttingDown || !desiredConnected) return;
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        if (authState.isAuthenticated()) {
            headers.setBearerAuth(authState.getToken());
        }

        stompClient.connectAsync(wsUrl, headers, new StompSessionHandlerAdapter() {

            @Override
            public void afterConnected(@NotNull StompSession session, @NotNull StompHeaders headers) {
                ShipmentStompClient.this.session = session;
                connected = true;

                session.subscribe(TOPIC_SHIPMENTS, frameHandler(lastTopicHandler));
                session.subscribe("/user" + QUEUE_SHIPMENTS, frameHandler(lastUserHandler));
            }

            @Override
            public void handleTransportError(@NotNull StompSession session, @NotNull Throwable exception) {
                connected = false;
                log.warn("WS transport error", exception);
                if (!shuttingDown) scheduleReconnect();
            }
        });
    }

    private void scheduleReconnect() {
        if (!desiredConnected || shuttingDown) return;
        reconnectExecutor.schedule(this::doConnect, 3, TimeUnit.SECONDS);
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
<<<<<<< ours

    public synchronized void disconnect() {
        requestDisconnect();
    }
=======
>>>>>>> theirs
}
