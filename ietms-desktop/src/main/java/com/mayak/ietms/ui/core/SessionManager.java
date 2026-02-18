package com.mayak.ietms.ui.core;

import com.mayak.ietms.infrastructure.error.AlertUtils;
import com.mayak.ietms.infrastructure.window.WindowService;
import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionManager {

    private final WindowService windowService;
    private volatile boolean logoutInProgress = false;

    public void handleSessionExpired() {
        if (logoutInProgress) return;
        logoutInProgress = true;

        Platform.runLater(() -> {
            AlertUtils.showWarning("Session expired!\nYou logged in from another device.");
            windowService.forceLogout();
            logoutInProgress = false;
        });
    }
}