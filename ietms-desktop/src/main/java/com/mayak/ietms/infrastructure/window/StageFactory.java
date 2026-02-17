package com.mayak.ietms.infrastructure.window;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class StageFactory {

    private static final String TITLE = "iETMS";
    private static final String ICON = "/icons/icon.png";

    public Stage createLoginStage(Scene scene) {
        Stage stage = baseStage(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        return stage;
    }

    public Stage createMainStage(Scene scene) {
        Stage stage = baseStage(scene);
        stage.setMaximized(true);
        return stage;
    }

    private Stage baseStage(Scene scene) {
        Stage stage = new Stage();
        stage.setTitle(TITLE);
        stage.setScene(scene);

        Image icon = new Image(Objects.requireNonNull(getClass().getResource(ICON)).toString());
        stage.getIcons().add(icon);

        return stage;
    }
}
