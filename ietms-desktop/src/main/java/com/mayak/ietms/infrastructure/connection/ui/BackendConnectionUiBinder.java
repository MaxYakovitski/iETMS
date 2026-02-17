package com.mayak.ietms.infrastructure.connection.ui;

import com.mayak.ietms.infrastructure.connection.BackendConnectionMonitor;
import com.mayak.ietms.infrastructure.window.WindowService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BackendConnectionUiBinder {

    private final BackendConnectionMonitor monitor;
    private final WindowService windowService;

    @PostConstruct
    public void bind() {
        monitor.stateProperty().addListener((obs, old, state) -> {
            switch (state) {
                case UNKNOWN -> windowService.showLoading();
                case CONNECTED -> windowService.hideBlockingOverlay();
                case DISCONNECTED -> windowService.showBackendUnavailable();
            }
        });
    }
}