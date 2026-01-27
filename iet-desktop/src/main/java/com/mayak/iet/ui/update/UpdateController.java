package com.mayak.iet.ui.update;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UpdateController {

    @FXML private Label messageLabel;
    @FXML private ProgressBar progressBar;

    @FXML
    private void initialize() {
        progressBar.setProgress(0);
    }

    public void showChecking() {
        runFx(() -> {
            messageLabel.setText("checking for updates…");
            progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        });
    }

    public void showDownloading() {
        runFx(() -> {
            messageLabel.setText("downloading update…");
            progressBar.setProgress(0);
        });
    }

    public void updateStatusMessage(String message) {
        runFx(() -> messageLabel.setText(message));
    }

    public void updateProgress(double progress) {
        runFx(() -> progressBar.setProgress(progress));
    }

    public void showFinalizing() {
        runFx(() -> {
            messageLabel.setText("finalizing update…");
            progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        });
    }

    public void showCompleted() {
        runFx(() -> {
            messageLabel.setText("application will restart now");
            progressBar.setProgress(1.0);
        });
    }

    public void showError(String message) {
        runFx(() -> {
            messageLabel.setText(message);
        });
    }

    public void showForcedUpdateError(String message) {
        runFx(() -> {
            messageLabel.setText(message);
            progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        });
    }

    private void runFx(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }
}
