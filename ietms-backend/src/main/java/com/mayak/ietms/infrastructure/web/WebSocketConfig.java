package com.mayak.ietms.infrastructure.web;

import com.mayak.ietms.infrastructure.security.websocket.JwtWebSocketInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket and STOMP message broker configuration.
 *
 * <p>Registers the {@code /ws} endpoint and configures the simple in-memory
 * broker for {@code /topic} and {@code /queue} destinations.
 *
 * <p>A {@link HandshakeInterceptor} copies the {@code Authorization} header
 * from the HTTP upgrade request into the WebSocket session attributes so that
 * {@link JwtWebSocketInterceptor} can authenticate the subsequent STOMP CONNECT
 * frame regardless of whether the client sends the token at the HTTP or STOMP level.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtWebSocketInterceptor jwtWebSocketInterceptor;

    @Value("${app.ws.allowed-origins}")
    private String[] allowedOrigins;

    /**
     * Registers the {@code /ws} STOMP endpoint with allowed origin patterns
     * from application properties.
     *
     * <p>Attaches a {@link HandshakeInterceptor} that forwards the
     * {@code Authorization} header from the HTTP upgrade request into the
     * WebSocket session attributes under the key {@code "Authorization"}.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins)
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(@NonNull ServerHttpRequest request,
                                                   @NonNull ServerHttpResponse response,
                                                   @NonNull WebSocketHandler wsHandler,
                                                   @NonNull Map<String, Object> attributes) {
                        String auth = request.getHeaders().getFirst("Authorization");
                        if (auth != null) {
                            attributes.put("Authorization", auth);
                        }
                        return true;
                    }

                    @Override
                    public void afterHandshake(@NonNull ServerHttpRequest request,
                                               @NonNull ServerHttpResponse response,
                                               @NonNull WebSocketHandler wsHandler,
                                               @Nullable Exception exception) {}
                });
    }

    /**
     * Configures the simple in-memory message broker.
     *
     * <ul>
     *   <li>{@code /topic} — broadcast destinations</li>
     *   <li>{@code /queue} — user-specific destinations</li>
     *   <li>{@code /app} — application destination prefix for {@code @MessageMapping}</li>
     *   <li>{@code /user} — user destination prefix for {@code convertAndSendToUser}</li>
     * </ul>
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * Registers {@link JwtWebSocketInterceptor} on the inbound channel
     * to authenticate every incoming STOMP CONNECT frame.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtWebSocketInterceptor);
    }
}