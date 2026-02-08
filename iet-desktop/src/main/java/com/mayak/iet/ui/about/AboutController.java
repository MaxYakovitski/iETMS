package com.mayak.iet.ui.about;

import com.mayak.iet.infrastructure.version.AppVersionProvider;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

@Controller
@Scope("prototype")
@RequiredArgsConstructor
public class AboutController {

    @FXML private Label versionLabel;
    private final AppVersionProvider versionProvider;

    @Getter
    private Stage stage;

    public void init(Stage stage) {
        this.stage = stage;
        versionLabel.setText("version: " + versionProvider.getAppVersion());
    }
}