package com.mayak.iet.app;

import com.mayak.iet.DesktopSpringBootApplication;
import com.mayak.iet.infrastructure.error.AlertUtils;
import com.mayak.iet.infrastructure.window.WindowService;
import com.mayak.iet.support.enums.View;
import com.mayak.iet.ui.auth.LoginController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Locale;
import java.util.Objects;

@Slf4j
public class JavaFxApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        springContext = new SpringApplicationBuilder(DesktopSpringBootApplication.class).headless(false).run();
        Locale.setDefault(Locale.UK);
    }

    @Override
    public void start(Stage primaryStage) {
        WindowService windowService = springContext.getBean(WindowService.class);
        windowService.setPrimaryStage(primaryStage);
        AlertUtils.setWindowService(windowService);

        try {
            WindowService.Loaded<LoginController> loaded =
                    windowService.loadControllerWithNode(View.LOGIN.getPath(), LoginController.class);

            Scene loginScene = new Scene(loaded.node());

            Image icon = new Image(Objects.requireNonNull(getClass().getResource("/icons/icon.png")).toString());
            primaryStage.getIcons().add(icon);
            primaryStage.setTitle("iETMS");

            primaryStage.setScene(loginScene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (Exception e) {
            log.error("Failed to load LoginController FXML", e);
        }
    }

    @Override
    public void stop() {
        springContext.close();
        Platform.exit();
    }
}
