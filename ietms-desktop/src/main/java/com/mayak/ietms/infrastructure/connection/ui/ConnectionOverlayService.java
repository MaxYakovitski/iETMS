package com.mayak.ietms.infrastructure.connection.ui;

import com.mayak.ietms.infrastructure.window.WindowService;
import com.mayak.ietms.ui.connection.ConnectionOverlayController;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Manages the connection status overlay displayed over the primary stage.
 * Shows loading or server-unavailable states based on backend connectivity.
 */
@Service
@RequiredArgsConstructor
public class ConnectionOverlayService {

    private final WindowService windowService;
    private Parent connectionOverlay;
    private ConnectionOverlayController connectionOverlayController;

    public void showLoading() {
        Platform.runLater(() -> {
            initOverlay();
            connectionOverlayController.showLoading();
            connectionOverlay.setVisible(true);
        });
    }

    public void showServerUnavailable() {
        Platform.runLater(() -> {
            windowService.closeAllDetachedWindows();
            windowService.bringPrimaryStageToFront();
            Platform.runLater(() -> {
                initOverlay();
                connectionOverlayController.showDisconnected();
                connectionOverlay.setVisible(true);
            });
        });
    }

    public void hide() {
        Platform.runLater(() -> {
            if (connectionOverlay != null) {
                connectionOverlay.setVisible(false);
            }
        });
    }

    private void initOverlay() {
        if (connectionOverlay != null) return;

        WindowService.Loaded<ConnectionOverlayController> loaded =
                windowService.loadControllerWithNode("/fxml/connection_overlay.fxml");

        connectionOverlay = loaded.node();
        connectionOverlayController = loaded.controller();

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