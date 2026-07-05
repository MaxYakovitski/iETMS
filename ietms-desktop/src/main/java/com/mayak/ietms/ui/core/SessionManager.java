package com.mayak.ietms.ui.core;

import com.mayak.ietms.auth.SessionService;
import com.mayak.ietms.infrastructure.error.AlertUtils;
import com.mayak.ietms.infrastructure.window.WindowService;
import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Reacts to session expiry (e.g. concurrent login on another device):
 * shows a warning and triggers session invalidation via SessionService.
 * The actual UI reset (closing windows, showing login) is handled by
 * AppNavigator, which reacts to the resulting SessionExpiredEvent.
 */
@Service
@RequiredArgsConstructor
public class SessionManager {

    private final WindowService windowService;
    private final SessionService sessionService;
    private volatile boolean logoutInProgress = false;

    /**
     * Triggers session invalidation via {@link SessionService#logoutExpired()} and
     * shows a warning alert. Idempotent — subsequent calls while a logout is
     * already in progress are ignored. May be called from any thread (e.g. the
     * scheduled ping in {@code BackendPingService}); only the alert itself is
     * dispatched onto the JavaFX application thread.
     */
    public void handleSessionExpired() {
        if (logoutInProgress) return;
        logoutInProgress = true;
        sessionService.logoutExpired();
        Platform.runLater(() ->
                AlertUtils.showWarning("Session expired!\nYou logged in from another device.",
                                                windowService.getPrimaryStage())
        );
    }
}
