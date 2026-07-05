package com.mayak.ietms.ui.auth;

import com.mayak.ietms.infrastructure.error.AlertUtils;
import com.mayak.ietms.infrastructure.common.TextUtils;
import com.mayak.ietms.infrastructure.error.ApiErrorUtils;
import com.mayak.ietms.integration.exception.ApiException;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.net.SocketException;
import java.util.function.Consumer;

/**
 * Controller for the login screen. Authentication is delegated via the
 * {@code onLogin} callback (set by {@link com.mayak.ietms.app.AppNavigator})
 * to keep this controller free of network logic.
 */
@Controller
@FxmlView("login.fxml")
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ProgressIndicator progress;

    @Setter
    private Consumer<LoginRequest> onLogin;

    @FXML
    public void initialize() {
        TextUtils.allowOnlyLatin(loginField, passwordField);
        loginField.setOnAction(e -> login());
        passwordField.setOnAction(e -> login());
    }

    /**
     * Enables or disables the loading state: disables input fields and
     * shows/hides the progress indicator.
     */
    public void setLoading(boolean loading) {
        loginField.setDisable(loading);
        passwordField.setDisable(loading);
        progress.setVisible(loading);
    }

    public void handleLoginError(Throwable ex) {
        setLoading(false);
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

    private void login() {
        String email = loginField.getText().trim();
        String password = passwordField.getText().trim();
        if (email.isEmpty() || password.isEmpty()) {
            AlertUtils.showError("Please enter both email and password!");
            return;
        }
        if (onLogin != null) {
            setLoading(true);
            onLogin.accept(new LoginRequest(email, password));
        }
    }
}
