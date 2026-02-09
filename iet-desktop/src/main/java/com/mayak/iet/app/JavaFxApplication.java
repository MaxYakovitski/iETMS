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
        DesktopLauncher.log("JavaFxApplication.init()");

        DesktopLauncher.log("Spring context init START");
        springContext = new AnnotationConfigApplicationContext(DesktopApplication.class);
        DesktopLauncher.log("Spring context init END");

        Locale.setDefault(Locale.UK);

        DesktopLauncher.log("JavaFxApplication.init() EXIT");
    }

    @Override
    public void start(Stage primaryStage) {
        DesktopLauncher.log("JavaFxApplication.start() ENTER");

        DesktopLauncher.log("Resolve WindowService");
        WindowService windowService = springContext.getBean(WindowService.class);
        windowService.setPrimaryStage(primaryStage);
        AlertUtils.setWindowService(windowService);

        DesktopLauncher.log("Stage setup START");
        Image icon = new Image(
                Objects.requireNonNull(getClass().getResource("/icons/icon.png")).toString()
        );
        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("iETMS");
        DesktopLauncher.log("Stage setup END");

        DesktopLauncher.log("DesktopBootstrap.start() START");
        springContext.getBean(DesktopBootstrap.class).start(primaryStage);
        DesktopLauncher.log("DesktopBootstrap.start() END");

        DesktopLauncher.log("JavaFxApplication.start() EXIT");
    }

    @Override
    public void stop() {
        DesktopLauncher.log("JavaFxApplication.stop()");
        springContext.close();
        Platform.exit();
    }
}
