package com.mayak.ietms.app;

import com.mayak.ietms.auth.SessionService;
import com.mayak.ietms.auth.event.LoginSucceededEvent;
import com.mayak.ietms.auth.event.SessionExpiredEvent;
import com.mayak.ietms.infrastructure.connection.ui.BackendConnectionUiBinder;
import com.mayak.ietms.infrastructure.error.AlertUtils;
import com.mayak.ietms.infrastructure.error.ApiErrorUtils;
import com.mayak.ietms.infrastructure.window.WindowService;
import com.mayak.ietms.ui.auth.LoginController;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Sole owner of the application's top-level screens (login / main window).
 *
 * <p>Shows the login screen at startup (once the Spring context is already
 * ready — see {@link JavaFxApplication#init()}, where context loading blocks
 * synchronously) and reacts to session lifecycle events: on successful login
 * ({@link com.mayak.ietms.auth.event.LoginSucceededEvent}) shows the main
 * window, on forced logout
 * ({@link com.mayak.ietms.auth.event.SessionExpiredEvent}) closes everything
 * and shows the login screen again. Replaces the previous callback mechanism
 * ({@code SessionManager.setLoginCallback}) — all navigation now goes through events.
 *
 * <p>Not to be confused with {@link com.mayak.ietms.ui.navigation.ContentNavigationService}:
 * that one is a per-session facade for navigation within the main window's
 * content area; this one is an application-scoped singleton for top-level screens.
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class AppNavigator {

    private static final String TITLE = "iETMS";
    private static final Image APP_ICON = new Image(
            Objects.requireNonNull(AppNavigator.class.getResource("/icons/icon.png")).toExternalForm());

    private final FxWeaver fxWeaver;
    private final WindowService windowService;
    private final DesktopBootstrap desktopBootstrap;
    private final SessionService sessionService;
    private final BackendConnectionUiBinder connectionUiBinder;

    private Stage mainStage;
    private Stage loginStage;

    public void showLogin() {
        var loaded = fxWeaver.load(LoginController.class);
        var controller = loaded.getController();
        Parent root = (Parent) loaded.getView().orElseThrow();

        loginStage = createBaseStage(new Scene(root));
        loginStage.setResizable(false);
        loginStage.centerOnScreen();

        controller.setOnLogin(req ->
                CompletableFuture.runAsync(() -> sessionService.login(req.email(), req.password())
                        ).exceptionally(ex -> {
                            Platform.runLater(() -> AlertUtils.show(ApiErrorUtils.resolve(ex)));
                            return null;
                })
        );
        loginStage.show();
    }

    public void showMain() {
        AlertUtils.setWindowService(windowService);

        DesktopBootstrap.StartupPlan plan = desktopBootstrap.buildInitialPlan();
        Stage stage = createBaseStage(plan.scene());
        plan.stageConfigurer().accept(stage);

        windowService.setPrimaryStage(stage);
        this.mainStage = stage;
        stage.show();
        connectionUiBinder.bind();
    }

    public void closeAll() {
        if (loginStage != null) {
            loginStage.close();
            loginStage = null;
        }
        if (mainStage != null) {
            mainStage.close();
            mainStage = null;
        }
    }

    @EventListener(LoginSucceededEvent.class)
    public void onLoginSucceeded() {
        Platform.runLater(() -> {
            if (loginStage != null) {
                loginStage.close();
                loginStage = null;
            }
            showMain();
        });
    }

    @EventListener(SessionExpiredEvent.class)
    public void onSessionExpired() {
        Platform.runLater(() -> {
            closeAll();
            showLogin();
        });
    }

    private Stage createBaseStage(Scene scene) {
        Stage stage = new Stage();
        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.getIcons().add(APP_ICON);
        return stage;
    }

}
