package com.mayak.ietms.infrastructure.update;

import com.mayak.ietms.infrastructure.util.OsUtils;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

/**
 * Centralised registry of file system paths used by the iETMS desktop application on Windows.
 * <p>
 * Installation paths are rooted at {@code %ProgramFiles%\iETMS},
 * user-data paths at {@code %LOCALAPPDATA%\iETMS}.
 * Non-Windows fallbacks are provided for development environments.
 */
@NoArgsConstructor
public final class AppPaths {

    private static final String APP_NAME = "iETMS";

    public static Path updaterExe() {
        return installDir()
                .resolve("app")
                .resolve("updater")
                .resolve("iETMS-Updater.exe");
    }

    public static Path updatesDir() {
        if (OsUtils.isWindows()) {
            return Path.of(System.getenv("LOCALAPPDATA"), APP_NAME, "updates");
        }
        return Path.of(System.getProperty("java.io.tmpdir"), APP_NAME, "updates");
    }

    public static Path logsDir() {
        if (OsUtils.isWindows()) {
            return Path.of(System.getenv("LOCALAPPDATA"), APP_NAME, "logs");
        }
        return Path.of(System.getProperty("user.home"), "." + APP_NAME.toLowerCase(), "logs");
    }

    public static Path msiFile(String version) {
        return updatesDir().resolve("iETMS-" + version + ".msi");
    }

    public static Path nativeHostExe() {
        return installDir()
                .resolve("app")
                .resolve("native-host")
                .resolve("iETMS-NativeHost.exe");
    }

    public static Path tokenFile() {
        return Path.of(System.getenv("LOCALAPPDATA"), APP_NAME, "native-host", "token");
    }

    public static Path nativeHostManifest() {
        return Path.of(System.getenv("LOCALAPPDATA"), APP_NAME, "native-host", "manifest.json");
    }

    private static Path installDir() {
        String programFiles = System.getenv("ProgramFiles");
        if (programFiles == null || programFiles.isBlank()) {
            throw new IllegalStateException("ProgramFiles env not found");
        }
        return Path.of(programFiles, APP_NAME);
    }
}