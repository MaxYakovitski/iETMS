package com.mayak.ietms.infrastructure.connection.ui;

import com.mayak.ietms.infrastructure.connection.BackendConnectionMonitor;
import com.mayak.ietms.infrastructure.connection.BackendConnectionState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BackendConnectionUiBinder {

    private final BackendConnectionMonitor monitor;
    private final ConnectionOverlayService overlayService;

    public void bind() {
        monitor.stateProperty().addListener((obs, old, state) -> handle(state));
        handle(monitor.getState());
    }

    private void handle(BackendConnectionState state) {
        switch (state) {
            case UNKNOWN -> overlayService.showLoading();
            case CONNECTED -> overlayService.hide();
            case DISCONNECTED -> overlayService.showServerUnavailable();
        }
    }

}