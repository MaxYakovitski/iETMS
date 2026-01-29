package com.mayak.iet.app;

import com.mayak.iet.infrastructure.update.UpdateCheckResult;
import com.mayak.iet.infrastructure.update.UpdateService;
import com.mayak.iet.infrastructure.window.WindowService;
import com.mayak.iet.support.enums.View;
import com.mayak.iet.ui.auth.LoginController;
import com.mayak.iet.ui.update.UpdateController;
import javafx.event.Event;
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
            showLogin(stage);
        }
    }

    private void showLogin(Stage stage) {
        WindowService.Loaded<LoginController> loaded =
                windowService.loadControllerWithNode(
                        View.LOGIN.getPath(),
                        LoginController.class);

        stage.setScene(new Scene(loaded.node()));
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    private void showUpdate(Stage stage, UpdateCheckResult result) {

        WindowService.Loaded<UpdateController> loaded =
                windowService.loadControllerWithNode(View.UPDATE.getPath(), UpdateController.class);

        UpdateController controller = loaded.controller();

        stage.setScene(new Scene(loaded.node()));
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();

        if (result.forced()) {
            stage.setOnCloseRequest(Event::consume);
        }

        controller.start(result);
    }
}
