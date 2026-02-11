package com.mayak.iet.infrastructure.update;

import com.mayak.iet.infrastructure.util.OsUtils;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

@NoArgsConstructor
public final class UpdatePaths {

    private static final String APP_NAME = "iETMS";

    public static Path installDir() {
        String programFiles = System.getenv("ProgramFiles");
        if (programFiles == null || programFiles.isBlank()) {
            throw new IllegalStateException("ProgramFiles env not found");
        }
        return Path.of(programFiles, APP_NAME);
    }

    public static Path updaterExe() {
        return installDir()
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
}