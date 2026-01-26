package com.mayak.iet.infrastructure.connection;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.springframework.stereotype.Service;

@Service
public class BackendConnectionMonitor {

    private final ObjectProperty<BackendConnectionState> state =
            new SimpleObjectProperty<>(BackendConnectionState.CONNECTED);

    public ObjectProperty<BackendConnectionState> stateProperty() {
        return state;
    }

    public BackendConnectionState getState() {
        return state.get();
    }

    public void markDisconnected(Throwable cause) {
        if (state.get() != BackendConnectionState.DISCONNECTED) {
            state.set(BackendConnectionState.DISCONNECTED);
        }
    }

    public void markConnected() {
        if (state.get() != BackendConnectionState.CONNECTED) {
            state.set(BackendConnectionState.CONNECTED);
        }
    }

}
