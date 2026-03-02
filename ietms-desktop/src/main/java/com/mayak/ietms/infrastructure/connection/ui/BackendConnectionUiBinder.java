package com.mayak.ietms.infrastructure.connection.ui;

import com.mayak.ietms.infrastructure.connection.BackendConnectionMonitor;
import com.mayak.ietms.infrastructure.connection.BackendConnectionState;
import com.mayak.ietms.infrastructure.window.WindowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BackendConnectionUiBinder {

    private final BackendConnectionMonitor monitor;
    private final WindowService windowService;

    public void bind() {
        monitor.stateProperty().addListener((obs, old, state) -> handle(state));
        handle(monitor.getState());
    }

    private void handle(BackendConnectionState state) {
        switch (state) {
            case UNKNOWN -> windowService.showLoading();
            case CONNECTED -> windowService.hideBlockingOverlay();
            case DISCONNECTED -> windowService.showBackendUnavailable();
        }
    }

}