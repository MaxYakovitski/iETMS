package com.mayak.iet.infrastructure.update;

import java.nio.file.Path;

public final class UpdatePaths {

    private static final String APP_NAME = "iETMS";

    private UpdatePaths() {
    }

    /** C:\Program Files\iETMS */
    public static Path installDir() {
        return Path.of(
                System.getenv("PROGRAMFILES"),
                APP_NAME
        );
    }

    public static Path appDir() {
        return installDir().resolve("app");
    }

    public static Path updaterJar() {
        return appDir()
                .resolve("updater")
                .resolve("iet-desktop-updater.jar");
    }

    public static Path updatesDir() {
        return Path.of(
                System.getenv("LOCALAPPDATA"),
                APP_NAME,
                "updates"
        );
    }

    public static Path msiFile(String version) {
        return updatesDir().resolve("iETMS-" + version + ".msi");
    }
}
