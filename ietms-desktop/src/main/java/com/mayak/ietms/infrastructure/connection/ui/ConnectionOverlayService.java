package com.mayak.ietms.infrastructure.connection.ui;

import com.mayak.ietms.infrastructure.window.WindowService;
import com.mayak.ietms.ui.connection.ConnectionOverlayController;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.stereotype.Service;

/**
 * Manages the connection status overlay displayed over the primary stage.
 * Shows loading or server-unavailable states based on backend connectivity.
 */
@Service
@RequiredArgsConstructor
public class ConnectionOverlayService {

    private final WindowService windowService;
    private final FxWeaver fxWeaver;
    private Parent connectionOverlay;
    private ConnectionOverlayController connectionOverlayController;

    /** Displays the loading spinner overlay over the primary stage. */
    public void showLoading() {
        Platform.runLater(() -> {
            initOverlay();
            connectionOverlayController.showLoading();
            connectionOverlay.setVisible(true);
        });
    }

    /** Closes all detached windows, brings the primary stage to front, then shows the disconnected state. */
    public void showServerUnavailable() {
        Platform.runLater(() -> {
            windowService.closeAllDetachedWindows();
            windowService.bringPrimaryStageToFront();
            // nested runLater ensures overlay init runs after closeAllDetachedWindows completes
            Platform.runLater(() -> {
                initOverlay();
                connectionOverlayController.showDisconnected();
                connectionOverlay.setVisible(true);
            });
        });
    }

    /** Hides the overlay. Has no effect if the overlay has not been initialised yet. */
    public void hide() {
        Platform.runLater(() -> {
            if (connectionOverlay != null) {
                connectionOverlay.setVisible(false);
            }
        });
    }

    private void initOverlay() {
        if (connectionOverlay != null) return;

        var loaded = fxWeaver.load(ConnectionOverlayController.class);
        connectionOverlay = (Parent) loaded.getView().orElseThrow();
        connectionOverlayController = loaded.getController();

        Scene scene = windowService.getPrimaryStage().getScene();
        Parent root = scene.getRoot();

        if (root instanceof StackPane stack) {
            stack.getChildren().add(connectionOverlay);
        } else {
            StackPane wrapper = new StackPane(root, connectionOverlay);
            scene.setRoot(wrapper);
        }
    }
}