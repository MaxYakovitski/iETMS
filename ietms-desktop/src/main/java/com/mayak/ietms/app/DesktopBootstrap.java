package com.mayak.ietms.app;

import com.mayak.ietms.infrastructure.update.UpdateCheckResult;
import com.mayak.ietms.infrastructure.update.UpdateService;
import com.mayak.ietms.infrastructure.window.FxmlLoader;
import com.mayak.ietms.infrastructure.window.WindowService;
import com.mayak.ietms.support.enums.View;
import com.mayak.ietms.ui.home.HomeController;
import com.mayak.ietms.ui.update.UpdateController;
import javafx.event.Event;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class DesktopBootstrap {

    private final UpdateService updateService;
    private final WindowService windowService;

    public record StartupPlan(Scene scene, Consumer<Stage> stageConfigurer) {}

    public StartupPlan buildInitialPlan() {
        UpdateCheckResult result = updateService.checkVersion();
        if (result.updateRequired()) {
            return buildUpdatePlan(result);
        }

        return buildHomePlan();
    }

    private StartupPlan buildHomePlan() {
        WindowService.Loaded<HomeController> loaded =
                windowService.loadControllerWithNode(View.HOME.getPath(), HomeController.class);

        Scene scene = new Scene(loaded.node());
        return new StartupPlan(scene, stage -> {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
        });
    }

    private StartupPlan buildUpdatePlan(UpdateCheckResult result) {
        UpdateController controller = new UpdateController();
        controller.setUpdateService(updateService);

        Parent root = FxmlLoader.load(View.UPDATE.getPath(), controller);
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
                    if (result.forced()) {
                        stage.setOnCloseRequest(Event::consume);
                    }
                }
        );
    }

}