package com.mayak.ietms.auth;

import com.mayak.ietms.auth.event.SessionClearedEvent;
import com.mayak.ietms.auth.event.TokenChangedEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Holds the in-memory state of the current session: access/refresh tokens.
 * Has no file I/O, HTTP, or UI logic — a pure data model that only publishes
 * events on mutation ({@link com.mayak.ietms.auth.event.TokenChangedEvent},
 * {@link com.mayak.ietms.auth.event.SessionClearedEvent}).
 *
 * <p><b>Mutate only via {@link #updateTokens} and {@link #clear}</b> — both
 * change state and publish the corresponding event atomically. Do not add a
 * class-level {@code @Setter} here: bare setters would let tokens change
 * while bypassing event publication and silently break listeners (e.g.
 * {@code NativeHostTokenSync} would stop refreshing the browser extension's
 * token files after a silent refresh — this has already happened once).
 */

@Component
@RequiredArgsConstructor
@Getter
public class SessionContext {

    private final ApplicationEventPublisher eventPublisher;

    private volatile String accessToken;
    private volatile String refreshToken;

    public boolean isAuthenticated() {
        return accessToken != null;
    }

    public void updateTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        eventPublisher.publishEvent(new TokenChangedEvent(accessToken, refreshToken));
    }

    public void clear() {
        this.accessToken = null;
        this.refreshToken = null;
        eventPublisher.publishEvent(new SessionClearedEvent());
    }
}
