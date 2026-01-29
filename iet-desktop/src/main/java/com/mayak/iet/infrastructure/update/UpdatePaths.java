package com.mayak.iet.infrastructure.update;

import java.nio.file.Path;

public final class UpdatePaths {

    private static final String APP_NAME = "iETMS";

    public static Path updatesDir() {
        String localAppData = System.getenv("LOCALAPPDATA");
        return Path.of(localAppData, APP_NAME, "updates");
    }

    public static Path msiFile(String version) {
        return updatesDir().resolve("iETMS-" + version + ".msi");
    }

    private UpdatePaths() {
    }
}
