package com.mayak.ietms.app;

import com.mayak.ietms.infrastructure.update.UpdateCheckResult;
import com.mayak.ietms.infrastructure.update.UpdateService;
import com.mayak.ietms.infrastructure.window.FxmlLoader;
import com.mayak.ietms.infrastructure.window.WindowService;
import com.mayak.ietms.support.enums.View;
import com.mayak.ietms.ui.home.HomeController;
import com.mayak.ietms.ui.update.UpdateController;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DesktopBootstrap {

    private final UpdateService updateService;
    private final WindowService windowService;

    public void start(Stage stage) {

        UpdateCheckResult result = updateService.checkVersion();

        if (result.updateRequired()) {
            showUpdate(stage, result);
        } else {
            showHome(stage);
        }
    }

    private void showHome(Stage stage) {
        WindowService.Loaded<HomeController> loaded =
                windowService.loadControllerWithNode(View.HOME.getPath(), HomeController.class);

        HomeController controller = loaded.controller();
        Scene scene = new Scene(loaded.node());
        stage.setScene(scene);

        windowService.injectStageIfSupported(controller, stage);
        windowService.setPrimaryStage(stage);

        stage.setMaximized(true);
        stage.show();
    }

    private void showUpdate(Stage stage, UpdateCheckResult result) {

        UpdateController controller = new UpdateController();
        controller.setUpdateService(updateService);

        Parent root = FxmlLoader.load(View.UPDATE.getPath(), controller);

        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.centerOnScreen();

        if (result.forced()) {
            stage.setOnCloseRequest(Event::consume);
        }

        stage.show();

        controller.start(result);
    }
}
