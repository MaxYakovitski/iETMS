package com.mayak.ietms.ui.about;

import com.mayak.ietms.infrastructure.version.AppVersionProvider;
import com.mayak.ietms.ui.core.ViewLifecycle;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

/**
 * Controller for the "About" screen.
 * Displays the current application version on show.
 */
@Controller
@FxmlView("about.fxml")
@Scope("prototype")
@RequiredArgsConstructor
public class AboutController implements ViewLifecycle {

    @FXML
    private Label versionLabel;

    private final AppVersionProvider versionProvider;

    @Getter @Setter
    private Stage stage;

    @Override
    public void onShow() {
        versionLabel.setText("version: " + versionProvider.getAppVersion());
    }
}