package com.mayak.ietms.auth;

import com.mayak.ietms.auth.event.LoginSucceededEvent;
import com.mayak.ietms.auth.event.LogoutEvent;
import com.mayak.ietms.auth.event.SessionExpiredEvent;
import com.mayak.ietms.integration.auth.AuthClient;
import com.mayak.ietms.integration.auth.dto.LoginResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Sole owner of the session lifecycle: initiates login/logout/forced-logout
 * and publishes the corresponding events
 * ({@link com.mayak.ietms.auth.event.LoginSucceededEvent},
 * {@link com.mayak.ietms.auth.event.LogoutEvent},
 * {@link com.mayak.ietms.auth.event.SessionExpiredEvent}).
 *
 * <p>Silent token refresh (401 → refresh → retry) is deliberately not part of
 * this class — it lives in the {@link com.mayak.ietms.config.RestClientConfig}
 * interceptor, which talks to {@link SessionContext} directly to avoid a
 * circular dependency (this service depends on
 * {@link com.mayak.ietms.integration.auth.AuthClient}, whose implementation is
 * itself a REST client that would otherwise depend back on the session layer).
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final AuthenticationService authenticationService;
    private final AuthClient authClient;
    private final SessionContext sessionContext;
    private final ApplicationEventPublisher eventPublisher;

    public void login(String email, String password) {
        LoginResponseDto response = authenticationService.authenticate(email, password);
        sessionContext.updateTokens(response.accessToken(), response.refreshToken());
        eventPublisher.publishEvent(new LoginSucceededEvent());
    }

    public void logout() {
        revokeQuietly();
        sessionContext.clear();
        eventPublisher.publishEvent(new LogoutEvent());
    }

    public void logoutExpired() {
        sessionContext.clear();
        eventPublisher.publishEvent(new SessionExpiredEvent());
    }

    private void revokeQuietly() {
        String refreshToken = sessionContext.getRefreshToken();
        if (refreshToken == null) return;
        try {
            authClient.logout(refreshToken);
        } catch (Exception e) {
            log.warn("[auth] Failed to revoke refresh token on logout", e);
        }
    }
}
