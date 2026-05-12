package com.mayak.ietms.app;

import com.mayak.ietms.infrastructure.update.UpdateCheckResult;
import com.mayak.ietms.infrastructure.update.UpdateService;
import com.mayak.ietms.infrastructure.window.FxmlLoader;
import com.mayak.ietms.ui.home.HomeController;
import com.mayak.ietms.ui.update.UpdateController;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Determines the initial startup plan for the desktop application.
 *
 * <p>On startup, checks whether an update is required. If so, returns a plan
 * that shows the update screen; otherwise returns a plan that opens the main
 * home view.
 *
 * @see StartupPlan
 */
@Component
@RequiredArgsConstructor
public class DesktopBootstrap {

    private final UpdateService updateService;
    private final FxWeaver fxWeaver;

    /**
     * Describes what to show on startup: the initial {@link Scene} and
     * a callback to configure the {@link Stage} (size, resizability, close policy).
     */
    public record StartupPlan(Scene scene, Consumer<Stage> stageConfigurer) {}

    /**
     * Checks the application version and returns the appropriate startup plan.
     * Delegates to {@link #buildUpdatePlan} if an update is required,
     * or {@link #buildHomePlan} otherwise.
     */
    public StartupPlan buildInitialPlan() {
        UpdateCheckResult result = updateService.checkVersion();
        if (result.updateRequired()) {
            return buildUpdatePlan(result);
        }
        return buildHomePlan();
    }

    private StartupPlan buildHomePlan() {
        var loaded = fxWeaver.load(HomeController.class);
        Scene scene = new Scene((Parent) loaded.getView().orElseThrow());
        return new StartupPlan(scene, stage -> stage.setMaximized(true));
    }

    private StartupPlan buildUpdatePlan(UpdateCheckResult result) {
        UpdateController controller = new UpdateController();
        controller.setUpdateService(updateService);

        Parent root = FxmlLoader.load(UpdateController.FXML, controller);
        Scene scene = new Scene(root);

        var started = new AtomicBoolean(false);
        scene.windowProperty().addListener((obs, oldWin, newWin) -> {
            if (newWin != null && started.compareAndSet(false, true)) {
                controller.start(result);
            }
        });

        return new StartupPlan(
                scene,
                stage -> {
                    stage.setResizable(false);
                    stage.centerOnScreen();
                    if (result.forced()) stage.setOnCloseRequest(Event::consume);
                }
        );
    }
}