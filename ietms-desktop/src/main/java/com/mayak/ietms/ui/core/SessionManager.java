package com.mayak.ietms.ui.core;

import com.mayak.ietms.infrastructure.error.AlertUtils;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

/**
 * Manages the user session lifecycle.
 * Handles session expiry and triggers forced logout.
 */
@Service
@RequiredArgsConstructor
public class SessionManager {

    private volatile boolean logoutInProgress = false;

    @Setter
    private Runnable loginCallback;

    /**
     * Triggers a forced logout due to session expiry (e.g. concurrent login on another device).
     * Idempotent — subsequent calls while logout is in progress are ignored.
     * Always executed on the JavaFX application thread.
     */
    public void handleSessionExpired() {
        if (logoutInProgress) return;
        logoutInProgress = true;

        Platform.runLater(() -> {
            AlertUtils.showWarning("Session expired!\nYou logged in from another device.");
            forceLogout();
        });
    }

    // closes all open stages before invoking the login callback to avoid stale window references
    private void forceLogout() {
        var windows = new ArrayList<>(Window.getWindows());
        for (Window w : windows) {
            if (w instanceof Stage s) {
                try { s.close(); } catch (Exception ignored) {}
            }
        }
        if (loginCallback != null) loginCallback.run();
    }
}