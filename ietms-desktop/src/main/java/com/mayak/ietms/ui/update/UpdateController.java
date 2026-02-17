package com.mayak.ietms.ui.update;

import com.mayak.ietms.infrastructure.update.UpdateCheckResult;
import com.mayak.ietms.infrastructure.update.UpdateListener;
import com.mayak.ietms.infrastructure.update.UpdateService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateController {

    @FXML private Label messageLabel;
    @FXML private ProgressBar progressBar;

    @Setter
    private UpdateService updateService;

    public void start(UpdateCheckResult result) {
        showChecking();

        if (!result.updateRequired()) {
            log.info("[UPDATE] no update required, current == latest");
            return;
        }

        if (result.forced()) {
            startMandatoryUpdate(result);
        }
    }

    private void startMandatoryUpdate(UpdateCheckResult result) {

        updateService.setListener(new UpdateListener() {

            @Override
            public void onStart(String current, String target) {
                updateStatusMessage("preparing download…");
            }

            @Override
            public void onMessage(String message) {
                updateStatusMessage(message);
            }

            @Override
            public void onProgress(double progress) {
                updateProgress(progress);
            }

            @Override
            public void onError(Throwable error) {
                showForcedUpdateError(
                        "This update is required to continue.\nPlease restart the application."
                );
            }
        });

        if (result.forced()) {
            updateService.startMandatoryUpdate(result);
        }
    }

    public void showChecking() {
        runFx(() -> {
            messageLabel.setText("checking for updates…");
            progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        });
    }

    public void updateStatusMessage(String message) {
        runFx(() -> messageLabel.setText(message));
    }

    public void updateProgress(double progress) {
        runFx(() -> progressBar.setProgress(progress));
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
