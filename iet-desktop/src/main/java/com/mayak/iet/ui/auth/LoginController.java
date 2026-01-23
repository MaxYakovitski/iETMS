package com.mayak.iet.ui.auth;

import com.mayak.iet.integration.auth.AuthClient;
import com.mayak.iet.support.enums.View;
import com.mayak.iet.integration.auth.AuthState;
import com.mayak.iet.ui.home.HomeController;
import com.mayak.iet.infrastructure.error.AlertUtils;
import com.mayak.iet.infrastructure.common.TextUtils;
import com.mayak.iet.infrastructure.window.WindowService;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;

    private final AuthClient authClient;
    private final AuthState authState;
    private final WindowService windowService;

    @FXML
    public void initialize() {
        TextUtils.allowOnlyLatin(true, loginField, passwordField);
        loginField.setOnAction(e -> login());
        passwordField.setOnAction(e -> login());
    }

    private void login() {
        String email = loginField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            AlertUtils.showError("Please enter both email and password!");
            return;
        }

        try {
            var response = authClient.login(email, password);
            authState.setToken(response.token());
            log.info("User '{}' successfully authenticated", email);

            openMainWindow();

        } catch (Exception e) {
            AlertUtils.showError("Invalid email or password");
        }
    }

    private void openMainWindow() {
        try {
            WindowService.Loaded<HomeController> loaded =
                    windowService.loadControllerWithNode(View.HOME.getPath(), HomeController.class);

            HomeController controller = loaded.controller();

            Stage stage = new Stage();
            Scene scene = new Scene(loaded.node());
            stage.setScene(scene);

            loaded.node().setOpacity(0);
            windowService.fadeIn(loaded.node(), 180);

            stage.setTitle("iETMS");
            stage.setMaximized(true);

            Image icon = new Image(Objects.requireNonNull(getClass().getResource("/icons/icon.png")).toString());
            stage.getIcons().add(icon);

            windowService.injectStageIfSupported(controller, stage);
            windowService.setPrimaryStage(stage);

            Stage loginStage = (Stage) loginField.getScene().getWindow();
            if (loginStage != null) loginStage.close();

            stage.show();
        } catch (Exception e) {
            log.error("Error while opening main window", e);
        }
    }
}