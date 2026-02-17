package com.mayak.iet.infrastructure.connection;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.springframework.stereotype.Service;

@Service
public class BackendConnectionMonitor {

    private final ObjectProperty<BackendConnectionState> state =
            new SimpleObjectProperty<>(BackendConnectionState.UNKNOWN);

    public ObjectProperty<BackendConnectionState> stateProperty() {
        return state;
    }

    public BackendConnectionState getState() {
        return state.get();
    }

    public void markDisconnected(Throwable cause) {
        updateState(BackendConnectionState.DISCONNECTED);
    }

    public void markConnected() {
        updateState(BackendConnectionState.CONNECTED);
    }

    private void updateState(BackendConnectionState newState) {
        if (state.get() == newState) return;
        Platform.runLater(() -> state.set(newState));
    }

}
