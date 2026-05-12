package com.mayak.ietms.ui.auth;

import com.mayak.ietms.infrastructure.error.AlertUtils;
import com.mayak.ietms.infrastructure.common.TextUtils;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.function.Consumer;

/**
 * Controller for the login screen.
 *
 * <p>Does not depend on the Spring context being fully initialised —
 * intentionally constructed manually in {@link com.mayak.ietms.app.JavaFxApplication}
 * before authentication completes. Authentication itself is delegated via
 * the {@code onLogin} callback to keep this controller free of network logic.
 */
@Controller
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    /** Classpath location of the login FXML, used by {@link com.mayak.ietms.app.JavaFxApplication}. */
    public static final String FXML = "/com/mayak/ietms/ui/auth/login.fxml";

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