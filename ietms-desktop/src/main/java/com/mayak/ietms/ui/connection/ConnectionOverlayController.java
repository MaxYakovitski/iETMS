package com.mayak.ietms.ui.connection;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

@Component
@FxmlView("connection_overlay.fxml")
public class ConnectionOverlayController {

    @FXML
    private Label messageLabel;

    @FXML
    private ProgressIndicator progress;

    public void showLoading() {
        messageLabel.setText("Connecting to server. \n Please wait…");
        progress.setVisible(true);
    }

    public void showDisconnected() {
        messageLabel.setText("Connection to server lost.\n Trying to reconnect…");
        progress.setVisible(true);
    }
}