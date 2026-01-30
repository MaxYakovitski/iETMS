package com.mayak.iet.infrastructure.update;

import java.nio.file.Path;

public final class UpdatePaths {

    private static final String APP_NAME = "iETMS";

    private UpdatePaths() {
    }

    public static Path baseDir() {
        String localAppData = System.getenv("LOCALAPPDATA");
        return Path.of(localAppData, APP_NAME);
    }

    public static Path updatesDir() {
        return baseDir().resolve("updates");
    }

    public static Path msiFile(String version) {
        return updatesDir().resolve("iETMS-" + version + ".msi");
    }

    public static Path updaterJar() {
        return baseDir().resolve("updater").resolve("iet-desktop-updater.jar");
    }
}
