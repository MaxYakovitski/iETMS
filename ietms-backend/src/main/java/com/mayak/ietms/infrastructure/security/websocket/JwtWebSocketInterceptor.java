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

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtWebSocketInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String authHeader = accessor.getFirstNativeHeader("Authorization");

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
