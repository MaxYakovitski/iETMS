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

/**
 * Utility class for displaying JavaFX alert dialogs.
 *
 * <p>Automatically binds the alert to the primary stage via {@link WindowService} when available.
 * Falls back to {@link Modality#APPLICATION_MODAL} with a custom icon if no stage is registered.
 *
 * <p>Usage:
 * <pre>{@code
 * AlertUtils.showInfo("Operation completed.");
 * AlertUtils.showError("Something went wrong.");
 * boolean confirmed = AlertUtils.showConfirmation("Delete", "Are you sure?");
 * AlertUtils.show(uiError);
 * }</pre>
 */

@Slf4j
@NoArgsConstructor
public class AlertUtils {

    @Setter private static WindowService windowService;

    /**
     * Shows an informational alert with the given message.
     */
    public static void showInfo(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Information", message);
    }

    public static void showInfo(String message, Stage owner) {
        showAlert(Alert.AlertType.INFORMATION, "Information", message, owner);
    }

    /**
     * Shows a warning alert with the given message.
     */
    public static void showWarning(String message) {
        showAlert(Alert.AlertType.WARNING, "Warning", message);
    }

    public static void showWarning(String message, Stage owner) {
        showAlert(Alert.AlertType.WARNING, "Warning", message, owner);
    }

    /**
     * Shows an error alert with the given message.
     */
    public static void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", message);
    }

    public static void showError(String message,  Stage owner) {
        showAlert(Alert.AlertType.ERROR, "Error", message,  owner);
    }

    /**
     * Shows a yes/no confirmation dialog.
     *
     * @return {@code true} if the user clicked "yes", {@code false} otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType yes = new ButtonType("yes");
        ButtonType no = new ButtonType("no");
        alert.getButtonTypes().setAll(yes, no);

        initOwner(alert);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yes;
    }

    /**
     * Dispatches a {@link UiError} to the appropriate alert type based on its severity.
     * Does nothing if {@code error} is {@code null}.
     */
    public static void show(UiError error) {
        if (error == null) return;

        switch (error.severity()) {
            case INFO    -> showInfo(error.message());
            case WARNING -> showWarning(error.message());
            case ERROR   -> showError(error.message());
        }
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        initOwner(alert);

        alert.getDialogPane().setStyle("-fx-background-color: #ffffff;");
        alert.showAndWait();
    }

    private static void showAlert(Alert.AlertType type, String title, String message, Stage owner) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (owner != null) {
            alert.initOwner(owner);
            alert.initModality(Modality.WINDOW_MODAL);
        } else {
            initOwner(alert);
        }
        alert.getDialogPane().setStyle("-fx-background-color: #ffffff;");
        alert.showAndWait();
    }

    private static void initOwner(Alert alert) {
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
}