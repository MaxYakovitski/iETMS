package com.mayak.iet.ui.auth;

import com.mayak.iet.integration.auth.AuthClient;
import com.mayak.iet.integration.auth.AuthState;
import com.mayak.iet.infrastructure.error.AlertUtils;
import com.mayak.iet.infrastructure.common.TextUtils;
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

@Controller
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private ProgressIndicator progress;

    @Setter
    private Consumer<LoginRequest> onLogin;

    @FXML
    public void initialize() {
        TextUtils.allowOnlyLatin(true, loginField, passwordField);
        loginField.setOnAction(e -> login());
        passwordField.setOnAction(e -> login());
    }

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