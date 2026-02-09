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
import java.util.concurrent.CompletableFuture;

@Slf4j
public class JavaFxApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        DesktopLauncher.log("JavaFxApplication.init()");
        Locale.setDefault(Locale.UK);
    }

    @Override
    public void start(Stage primaryStage) {
        DesktopLauncher.log("JavaFxApplication.start() ENTER");

        setupStage(primaryStage);

        CompletableFuture
                .supplyAsync(this::initSpring)
                .thenAccept(ctx ->
                        Platform.runLater(() -> onSpringReady(ctx, primaryStage)));

        DesktopLauncher.log("JavaFxApplication.start() ENTER");
    }

    private ConfigurableApplicationContext initSpring() {
        DesktopLauncher.log("Spring context init START");
        ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext(DesktopApplication.class);
        DesktopLauncher.log("Spring context init END");
        return ctx;
    }

    private void onSpringReady(ConfigurableApplicationContext ctx, Stage stage) {
        DesktopLauncher.log("Spring ready – wiring UI");

        this.springContext = ctx;

        WindowService windowService = ctx.getBean(WindowService.class);
        windowService.setPrimaryStage(stage);
        AlertUtils.setWindowService(windowService);

        DesktopLauncher.log("DesktopBootstrap.start() START");
        ctx.getBean(DesktopBootstrap.class).start(stage);
        DesktopLauncher.log("DesktopBootstrap.start() END");
    }

    private void setupStage(Stage stage) {
        DesktopLauncher.log("Stage setup START");

        Image icon = new Image(Objects.requireNonNull(getClass().getResource("/icons/icon.png")).toString());
        stage.getIcons().add(icon);
        stage.setTitle("iETMS");

        DesktopLauncher.log("Stage setup END");
    }

    @Override
    public void stop() {
        DesktopLauncher.log("JavaFxApplication.stop()");
        springContext.close();
        Platform.exit();
    }
}
