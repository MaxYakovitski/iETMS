package com.mayak.ietms.integration.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mayak.ietms.app.BackendProperties;
import com.mayak.ietms.request.dto.event.ShipmentEventDto;
import com.mayak.ietms.shipment.event.ShipmentEvent;
import com.mayak.ietms.integration.auth.AuthState;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.mayak.ietms.ws.WsDestinations.QUEUE_SHIPMENTS;

@Component
@Slf4j
public class ShipmentStompClient extends AbstractStompClient {

    private static final TypeReference<ShipmentEvent<ShipmentEventDto>> EVENT_TYPE =
            new TypeReference<>() {};

    private final WebSocketStompClient stompClient;
    private final ObjectMapper mapper;
    private final String wsUrl;
    private final AuthState authState;

    private final List<Consumer<ShipmentEvent<ShipmentEventDto>>> handlers = new CopyOnWriteArrayList<>();

    public ShipmentStompClient(AuthState authState, BackendProperties backendProperties) {
        this.authState = authState;
        this.wsUrl = backendProperties.getWsUrl();
        this.stompClient = buildStompClient();
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    /**
     * Public API.
     * Declares intent that WS must stay connected.
     */
    public Runnable connect(Consumer<ShipmentEvent<ShipmentEventDto>> onEvent) {
        handlers.add(onEvent);
        requestConnect();
        if (!connected && !shuttingDown) {
            doConnect();
        }
        return () -> {
            handlers.remove(onEvent);
            if (handlers.isEmpty()) requestDisconnect();
        };
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

                session.subscribe("/user" + QUEUE_SHIPMENTS, frameHandler(e -> handlers.forEach(h -> h.accept(e))));
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

    private StompFrameHandler frameHandler(Consumer<ShipmentEvent<ShipmentEventDto>> consumer) {
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
}