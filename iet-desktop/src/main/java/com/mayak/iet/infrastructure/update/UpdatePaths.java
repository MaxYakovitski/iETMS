package com.mayak.iet.infrastructure.update;

import com.mayak.iet.infrastructure.util.OsUtils;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

@NoArgsConstructor
public final class UpdatePaths {

    private static final String APP_NAME = "iETMS";

    public static Path installDir() {
        return Path.of(System.getenv("PROGRAMFILES"), APP_NAME);
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
        if (OsUtils.isWindows()) {
            return Path.of(System.getenv("LOCALAPPDATA"), APP_NAME, "updates");
        }

        return Path.of(System.getProperty("java.io.tmpdir"), APP_NAME, "updates");
    }

    public static Path msiFile(String version) {
        return updatesDir().resolve("iETMS-" + version + ".msi");
    }
}