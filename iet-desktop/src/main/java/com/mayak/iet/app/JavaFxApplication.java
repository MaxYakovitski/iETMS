package com.mayak.iet.app;

import com.mayak.iet.DesktopApplication;
import com.mayak.iet.infrastructure.error.AlertUtils;
import com.mayak.iet.infrastructure.window.WindowService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Locale;
import java.util.Objects;

@Slf4j
public class JavaFxApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        springContext = new AnnotationConfigApplicationContext(DesktopApplication.class);
        Locale.setDefault(Locale.UK);
    }

    @Override
    public void start(Stage primaryStage) {
        WindowService windowService = springContext.getBean(WindowService.class);
        windowService.setPrimaryStage(primaryStage);
        AlertUtils.setWindowService(windowService);

            Image icon = new Image(Objects.requireNonNull(getClass().getResource("/icons/icon.png")).toString());
            primaryStage.getIcons().add(icon);
            primaryStage.setTitle("iETMS");

            springContext.getBean(DesktopBootstrap.class).start(primaryStage);
    }

    @Override
    public void stop() {
        springContext.close();
        Platform.exit();
    }
}
