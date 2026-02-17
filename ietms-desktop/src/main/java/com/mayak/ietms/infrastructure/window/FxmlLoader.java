package com.mayak.ietms.infrastructure.window;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

@NoArgsConstructor
public class FxmlLoader {

    public static Parent load(String fxmlPath, Object controller) {
        try {
            URL url = Objects.requireNonNull(FxmlLoader.class.getResource(fxmlPath),
                    "FXML not found: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(url);
            loader.setController(controller);
            return loader.load();

        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + fxmlPath, e);
        }
    }
}