package com.mayak.iet.ui.about;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

@Controller
@Scope("prototype")
public class AboutController {

    @FXML private Label versionLabel;

    @Getter
    private Stage stage;

    public void init(Stage stage) {
        this.stage = stage;
        versionLabel.setText("Version: " + resolveVersion());
    }

    private String resolveVersion() {
        // 1. Try Implementation-Version
        Package pkg = getClass().getPackage();
        if (pkg != null && pkg.getImplementationVersion() != null) {
            return pkg.getImplementationVersion();
        }

        // 2. Fallback to App-Version from MANIFEST
        try {
            var resources = getClass()
                    .getClassLoader()
                    .getResources("META-INF/MANIFEST.MF");

            while (resources.hasMoreElements()) {
                try (var is = resources.nextElement().openStream()) {
                    var manifest = new java.util.jar.Manifest(is);
                    String v = manifest.getMainAttributes().getValue("App-Version");
                    if (v != null) return v;
                }
            }
        } catch (Exception ignored) {
        }

        // 3. IDE / dev mode
        return "dev";
    }
}