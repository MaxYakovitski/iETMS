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

    @Setter private Runnable loginCallback;

    public void handleSessionExpired() {
        if (logoutInProgress) return;
        logoutInProgress = true;

        Platform.runLater(() -> {
            AlertUtils.showWarning("Session expired!\nYou logged in from another device.");
            forceLogout();
        });
    }

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