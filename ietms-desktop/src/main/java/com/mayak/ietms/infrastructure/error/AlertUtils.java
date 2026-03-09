package com.mayak.ietms.infrastructure.error;

import com.mayak.ietms.infrastructure.window.WindowService;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@NoArgsConstructor
public class AlertUtils {

    @Setter private static WindowService windowService;

    // =========================================================
    // PUBLIC API — SIMPLE ALERTS (old API, backward compatible)
    // =========================================================
    public static void showInfo(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Information", message, null);
    }

    public static void showWarning(String message) {
        showAlert(Alert.AlertType.WARNING, "Warning message", message, null);
    }

    public static void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Error message", message, null);
    }

    public static boolean showConfirmation(String title, String message) {
        return showConfirmationInternal(title, message);
    }


    // =========================================================
    // PUBLIC API — ALERTS WITH OWNER (recommended for controllers)
    // =========================================================
    public static void showInfo(String message, Stage owner) {
        showAlert(Alert.AlertType.INFORMATION, "Information", message, owner);
    }

    public static void showWarning(String message, Stage owner) {
        showAlert(Alert.AlertType.WARNING, "Warning", message, owner);
    }

    public static void showError(String message, Stage owner) {
        showAlert(Alert.AlertType.ERROR, "Error", message, owner);
    }


    // =========================================================
    // INTERNAL: CONFIRMATION HANDLER
    // =========================================================
    private static boolean showConfirmationInternal(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType yes = new ButtonType("yes");
        ButtonType no = new ButtonType("no");
        alert.getButtonTypes().setAll(yes, no);

        initOwner(alert, null);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yes;
    }


    // =========================================================
    // INTERNAL: UNIVERSAL ALERT BUILDER
    // =========================================================
    private static void showAlert(Alert.AlertType type, String title, String message, Stage owner) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        initOwner(alert, owner);

        alert.getDialogPane().setStyle("-fx-background-color: #ffffff;");
        alert.showAndWait();
    }


    // =========================================================
    // INTERNAL: OWNER LOGIC
    // =========================================================
    private static void initOwner(Alert alert, Stage owner) {

        if (owner != null) {
            alert.initOwner(owner);
            alert.initModality(Modality.WINDOW_MODAL);
            return;
        }

        if (windowService != null && windowService.getPrimaryStage() != null) {
            alert.initOwner(windowService.getPrimaryStage());
            alert.initModality(Modality.WINDOW_MODAL);
            return;
        }

        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (isShowing) {
                Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                try {
                    Image icon = new Image(Objects.requireNonNull(AlertUtils.class.getResourceAsStream("/icons/icon-red.png")));
                    alertStage.getIcons().setAll(icon);
                } catch (Exception ignored) {}
            }
        });
    }

    // =========================================================
    // UI ERROR DISPATCH
    // =========================================================
    public static void show(UiError error) {
        show(error, null);
    }

    public static void show(UiError error, Stage owner) {
        if (error == null) return;

        switch (error.severity()) {
            case INFO -> showInfo(error.message(), owner);
            case WARNING -> showWarning(error.message(), owner);
            case ERROR -> showError(error.message(), owner);
        }
    }
}