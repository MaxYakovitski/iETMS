package com.mayak.iet.infrastructure.tost;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.NoArgsConstructor;

import java.util.Objects;

@NoArgsConstructor
public final class ToastService {

    private static final Duration FADE_IN = Duration.millis(200);
    private static final Duration FADE_OUT = Duration.millis(300);

    public static void showInfo(Stage owner, String title, String message) {
        if (owner == null || !owner.isShowing()) return;

        Platform.runLater(() -> {
            Popup popup = new Popup();
            popup.setAutoFix(true);
            popup.setAutoHide(false);

            VBox content = buildContent(title, message, () -> {});

            popup.getContent().add(content);
            popup.show(owner);

            positionBottomRight(owner, popup, content);

            FadeTransition fadeIn = new FadeTransition(FADE_IN, content);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            FadeTransition fadeOut = new FadeTransition(FADE_OUT, content);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> popup.hide());

            attachCloseHandler(content, fadeOut::play);

            fadeIn.play();
        });
    }

    private static VBox buildContent(String title, String message, Runnable onClose) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("""
            -fx-font-weight: bold;
            -fx-text-fill: white;
            -fx-font-size: 13px;
        """);

        Label messageLabel = new Label(message);
        messageLabel.setStyle("""
            -fx-text-fill: #e0e0e0;
            -fx-font-size: 12px;
        """);
        messageLabel.setWrapText(true);

        Label closeLabel = new Label();
        closeLabel.setPrefSize(12, 12);
        closeLabel.setMinSize(12, 12);
        closeLabel.setMaxSize(12, 12);

        closeLabel.setBackground(new Background(
                new BackgroundImage(
                        new Image(Objects.requireNonNull(ToastService.class.getResource("/icons/refuse.png")).toExternalForm()),
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(12, 12, false, false, false, false)
                )
        ));
        closeLabel.setCursor(Cursor.HAND);


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(6, titleLabel, spacer, closeLabel);

        VBox box = new VBox(6, header, messageLabel);
        box.setPadding(new Insets(10));
        box.setMaxWidth(320);
        box.setOpacity(0);

        box.setBackground(new Background(
                new BackgroundFill(Color.rgb(40, 40, 40, 0.95), new CornerRadii(6), Insets.EMPTY)
        ));

        box.setBorder(new Border(
                new BorderStroke(
                Color.rgb(80, 80, 80), BorderStrokeStyle.SOLID, new CornerRadii(6), BorderWidths.DEFAULT)));

        return box;
    }

    private static void attachCloseHandler(VBox content, Runnable onClose) {
        HBox header = (HBox) content.getChildren().getFirst();
        Label closeLabel = (Label) header.getChildren().get(2);
        closeLabel.setOnMouseClicked(e -> onClose.run());
    }

    private static void positionBottomRight(Stage owner, Popup popup, Region content) {
        Scene scene = owner.getScene();
        if (scene == null) return;

        double padding = 16;

        popup.setX(owner.getX() + scene.getWidth() - content.getWidth() - padding);
        popup.setY(owner.getY() + scene.getHeight() - content.getHeight() - padding);
    }
}
