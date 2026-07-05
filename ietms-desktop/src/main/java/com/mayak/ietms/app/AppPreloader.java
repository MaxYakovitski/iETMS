package com.mayak.ietms.app;

import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Splash screen shown automatically by the JavaFX runtime before
 * {@link JavaFxApplication#init()} is called — activated via the system
 * property {@code -Djavafx.preloader=com.mayak.ietms.app.AppPreloader}
 * (see pom.xml, {@code windows-installer} profile, and the IntelliJ run
 * configuration's VM options for local development).
 *
 * <p>Hides itself upon receiving {@code StateChangeNotification.Type.BEFORE_START}
 * — the FX runtime sends this notification automatically right before
 * {@link JavaFxApplication#start} is invoked, meaning the Spring context is
 * guaranteed to be fully built by that point.
 */

public class AppPreloader extends Preloader {

    private Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;

        var title = new Label("Welcome to iETMS");
        title.setOpacity(0.5);
        title.setFont(Font.font(24));
        var root = new VBox(title);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("app-root");

        var scene = new Scene(root, 480, 240);
        scene.getStylesheets().add("/css/styles.css");

        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public void handleStateChangeNotification(StateChangeNotification scn) {
        if (scn.getType() == StateChangeNotification.Type.BEFORE_START) {
            stage.hide();
        }
    }
}
