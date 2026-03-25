package com.mayak.ietms.infrastructure.security.websocket;

import com.mayak.ietms.infrastructure.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * STOMP channel interceptor that authenticates WebSocket CONNECT frames.
 *
 * <p>Looks for a JWT token in two places (in order):
 * <ol>
 *   <li>STOMP CONNECT native header {@code Authorization}</li>
 *   <li>WebSocket handshake session attributes — populated by
 *       {@code WebSocketConfig} from the HTTP upgrade request headers</li>
 * </ol>
 *
 * <p>If no valid token is found the connection is rejected by throwing
 * {@link IllegalArgumentException}, which causes Spring to close the session.
 * If the token is present but invalid (expired, malformed) the connection
 * is also rejected.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtWebSocketInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    /**
     * Intercepts inbound STOMP frames and enforces authentication on CONNECT.
     *
     * <p>Non-CONNECT frames pass through without any processing.
     * On successful authentication the resolved user ID is set as the
     * STOMP session principal via {@link StompHeaderAccessor#setUser}.
     *
     * @param message the inbound STOMP message
     * @param channel the message channel
     * @return the original message if authentication succeeds or the frame
     *         is not a CONNECT command
     * @throws IllegalArgumentException if the Authorization header is missing
     *                                  or the JWT token is invalid
     */
    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader == null) {
            Map<String, Object> attrs = accessor.getSessionAttributes();
            if (attrs != null) {
                authHeader = (String) attrs.get("Authorization");
            }
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("WS CONNECT rejected: missing or invalid Authorization header");
            throw new IllegalArgumentException("Missing Authorization header");
        }

        try {
            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);
            accessor.setUser(() -> String.valueOf(userId));
            log.debug("WS CONNECT authenticated userId={}", userId);
        } catch (Exception e) {
            log.warn("WS CONNECT rejected: JWT parse failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT token");
        }

        return message;
    }
}