package com.mayak.ietms.app;

import com.mayak.ietms.DesktopApplication;
import com.mayak.ietms.infrastructure.common.SlackErrorReporter;
import com.mayak.ietms.infrastructure.connection.ui.BackendConnectionUiBinder;
import com.mayak.ietms.infrastructure.error.AlertUtils;
import com.mayak.ietms.infrastructure.error.ApiErrorUtils;
import com.mayak.ietms.infrastructure.window.FxmlLoader;
import com.mayak.ietms.infrastructure.window.WindowService;
import com.mayak.ietms.integration.auth.AuthClient;
import com.mayak.ietms.integration.auth.AuthState;
import com.mayak.ietms.integration.exception.ApiException;
import com.mayak.ietms.integration.exception.SessionExpiredException;
import com.mayak.ietms.ui.auth.LoginController;
import com.mayak.ietms.ui.auth.LoginRequest;
import com.mayak.ietms.ui.core.SessionManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.net.SocketException;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * JavaFX entry point for the desktop application.
 *
 * <p>Bootstraps the Spring context asynchronously in {@link #init()}
 * to avoid blocking the FX thread. Authentication is performed before
 * the context is fully wired into the UI ({@link #showMainStage}).
 *
 * <p>On window close, the active refresh token is revoked before
 * the Spring context is shut down to invalidate the server-side session.
 */
@Slf4j
public class JavaFxApplication extends Application {

    private static final String TITLE = "iETMS";
    private static final Image APP_ICON = new Image(
            Objects.requireNonNull(JavaFxApplication.class.getResource("/icons/icon.png")).toExternalForm());

    private ConfigurableApplicationContext springContext;
    private CompletableFuture<ConfigurableApplicationContext> contextFuture;

    private Stage mainStage;

    @Override
    public void init() {
        Locale.setDefault(Locale.UK);
        contextFuture = CompletableFuture.supplyAsync(() ->
                new AnnotationConfigApplicationContext(DesktopApplication.class));
    }

    @Override
    public void start(Stage primaryStage) {
        showLogin();
    }

    /**
     * Called by the JavaFX runtime on application shutdown.
     * Closes the main stage and the Spring context; if the context is still
     * loading, schedules its closure via the {@code contextFuture}.
     */
    @Override
    public void stop() {
        if (mainStage != null) {
            mainStage.close();
            mainStage = null;
        }

        if (springContext != null) {
            try {
                AuthState authState = springContext.getBean(AuthState.class);
                AuthClient authClient = springContext.getBean(AuthClient.class);
                if (authState.getRefreshToken() != null) {
                    authClient.logout(authState.getRefreshToken());
                }
            } catch (Exception ignored) {}
            springContext.close();
            springContext = null;
        } else if (contextFuture != null) {
            contextFuture.thenAccept(ConfigurableApplicationContext::close);
        }
        Platform.exit();
    }

    private Stage createBaseStage(Scene scene) {
        Objects.requireNonNull(scene, "Scene must not be null");

        Stage stage = new Stage();
        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.getIcons().add(APP_ICON);
        return stage;
    }

    private void showLogin() {
        LoginController controller = new LoginController();
        Parent root = FxmlLoader.load(LoginController.FXML, controller);
        Scene scene = new Scene(root);

        Stage loginStage = createBaseStage(scene);
        loginStage.setResizable(false);
        loginStage.centerOnScreen();

        controller.setOnLogin(req ->
                contextFuture
                        .thenApplyAsync(ctx -> authenticate(ctx, req))
                        .thenAccept(ctx -> Platform.runLater(() -> {
                            loginStage.close();
                            showMainStage(ctx);
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> handleLoginError(controller, ex));
                            return null;
                        })
        );
        loginStage.show();
    }

    /**
     * Performs login against the backend and stores the received accessToken and refreshToken in {@link AuthState}.
     * Runs on a background thread (via {@link CompletableFuture}).
     *
     * @return the same {@code ctx} for chaining
     * @throws com.mayak.ietms.integration.exception.ApiException on invalid credentials or server error
     */
    private ConfigurableApplicationContext authenticate(ConfigurableApplicationContext ctx, LoginRequest req) {
        var authClient = ctx.getBean(AuthClient.class);
        var authState  = ctx.getBean(AuthState.class);
        var response = authClient.login(req.email(), req.password());
        authState.setToken(response.accessToken());
        authState.setRefreshToken(response.refreshToken());
        return ctx;
    }

    /**
     * Wires the Spring context into the UI after successful login.
     *
     * <p>Registers a logout callback on {@link SessionManager} that closes
     * the current context and re-enters the login flow. Also installs a global
     * uncaught exception handler that routes {@link SessionExpiredException}
     * to the session manager and reports everything else to Slack.
     */
    private void showMainStage(ConfigurableApplicationContext ctx) {
        this.springContext = ctx;

        WindowService windowService = ctx.getBean(WindowService.class);
        SessionManager sessionManager = ctx.getBean(SessionManager.class);
        sessionManager.setLoginCallback(() -> {
            if (mainStage != null) {
                mainStage.close();
                mainStage = null;
            }
            if (springContext != null) {
                springContext.close();
                springContext = null;
            }
            contextFuture = CompletableFuture.supplyAsync(() -> new AnnotationConfigApplicationContext(DesktopApplication.class));
            showLogin();
        });

        AlertUtils.setWindowService(windowService);

        DesktopBootstrap bootstrap = ctx.getBean(DesktopBootstrap.class);
        DesktopBootstrap.StartupPlan plan = bootstrap.buildInitialPlan();

        Stage stage = createBaseStage(plan.scene());
        plan.stageConfigurer().accept(stage);

        windowService.setPrimaryStage(stage);
        this.mainStage = stage;
        stage.show();

        BackendConnectionUiBinder binder = ctx.getBean(BackendConnectionUiBinder.class);
        binder.bind();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Throwable t = throwable;
            while (t.getCause() != null) t = t.getCause();

            if (t instanceof SessionExpiredException) {
                sessionManager.handleSessionExpired();
                return;
            }

            log.error("Unhandled exception", throwable);
            SlackErrorReporter reporter = ctx.getBean(SlackErrorReporter.class);
            if (t instanceof Exception e) {
                reporter.report(e, "Uncaught exception in thread " + thread.getName());
            } else {
                reporter.report(new Exception(t), "Uncaught throwable in thread " + thread.getName());
            }
        });
    }

    private void handleLoginError(LoginController controller, Throwable ex) {
        controller.setLoading(false);

        Throwable t = ex;
        while (t.getCause() != null) t = t.getCause();
        if (t instanceof ApiException apiEx) {
            AlertUtils.show(ApiErrorUtils.resolve(apiEx, "Invalid email or password."));
        } else if (t instanceof SocketException) {
            AlertUtils.showError("No internet connection or cannot reach server. Please check your network!");
        } else {
            log.error("[login] Unexpected error", ex);
            AlertUtils.showError("Login failed. Please try again.");
        }
    }
}