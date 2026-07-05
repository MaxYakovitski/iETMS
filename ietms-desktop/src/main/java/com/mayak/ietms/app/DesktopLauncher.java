package com.mayak.ietms.app;

import com.mayak.ietms.infrastructure.update.AppPaths;
import javafx.application.Application;

public class DesktopLauncher {

    public static void main(String[] args) {
        System.setProperty("ietms.logs.dir", AppPaths.logsDir().toString());
        Application.launch(JavaFxApplication.class, args);
    }
}
