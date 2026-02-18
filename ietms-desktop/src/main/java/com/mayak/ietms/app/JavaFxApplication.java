package com.mayak.ietms.app;

import com.mayak.ietms.DesktopApplication;
import com.mayak.ietms.infrastructure.error.AlertUtils;
import com.mayak.ietms.infrastructure.error.ApiErrorUtils;
import com.mayak.ietms.infrastructure.window.FxmlLoader;
import com.mayak.ietms.infrastructure.window.StageFactory;
import com.mayak.ietms.infrastructure.window.WindowService;
import com.mayak.ietms.integration.auth.AuthClient;
import com.mayak.ietms.integration.auth.AuthState;
import com.mayak.ietms.integration.exception.ApiException;
import com.mayak.ietms.integration.exception.SessionExpiredException;
import com.mayak.ietms.support.enums.View;
import com.mayak.ietms.ui.auth.LoginController;
import com.mayak.ietms.ui.auth.LoginRequest;
import com.mayak.ietms.ui.core.SessionManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class JavaFxApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        Locale.setDefault(Locale.UK);
    }

    @Override
    public void start(Stage primaryStage) {
        showLogin();
    }

    private void showLogin() {
        LoginController controller = new LoginController();
        Parent root = FxmlLoader.load(View.LOGIN.getPath(), controller);
        Scene scene = new Scene(root);

        StageFactory stageFactory = new StageFactory();
        Stage loginStage = stageFactory.createLoginStage(scene);

        controller.setOnLogin(req ->
                CompletableFuture
                        .supplyAsync(() -> initSpringAndLogin(req))
                        .thenAccept(ctx ->
                                Platform.runLater(() -> {
                                    loginStage.close();
                                    showMainStage(ctx);
                                })
                        )
                        .exceptionally(ex -> {
                            Platform.runLater(() -> handleLoginError(controller, ex));
                            return null;
                        })
        );

        loginStage.show();

    }

    private ConfigurableApplicationContext initSpringAndLogin(LoginRequest req) {
        ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext(DesktopApplication.class);

        var authClient = ctx.getBean(AuthClient.class);
        var authState  = ctx.getBean(AuthState.class);

        try {
            var response = authClient.login(req.email(), req.password());
            authState.setToken(response.token());
            return ctx;
        } catch (ApiException ex) {
            ctx.close();
            throw ex;
        }
    }

    private void showMainStage(ConfigurableApplicationContext ctx) {
        this.springContext = ctx;

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Throwable t = throwable;
            while (t.getCause() != null) t = t.getCause();

            if (t instanceof SessionExpiredException) {
                ctx.getBean(SessionManager.class).handleSessionExpired();
                return;
            }

            log.error("Unhandled exception", throwable);
        });

        WindowService windowService = ctx.getBean(WindowService.class);
        AlertUtils.setWindowService(windowService);

        Parent root = ctx.getBean(WindowService.class)
                .loadControllerWithNode(View.HOME.getPath()).node();

        Scene scene = new Scene(root);

        StageFactory stageFactory = ctx.getBean(StageFactory.class);
        Stage mainStage = stageFactory.createMainStage(scene);

        windowService.setPrimaryStage(mainStage);
        ctx.getBean(DesktopBootstrap.class).start(mainStage);
    }

    private void handleLoginError(LoginController controller, Throwable ex) {
        controller.setLoading(false);

        Throwable t = ex;
        while (t.getCause() != null) t = t.getCause();

        if (t instanceof ApiException apiEx) {
            AlertUtils.show(ApiErrorUtils.resolve(apiEx, "Invalid email or password."));
        } else {
            AlertUtils.showError("Login failed. Please try again.");
        }
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }
}
