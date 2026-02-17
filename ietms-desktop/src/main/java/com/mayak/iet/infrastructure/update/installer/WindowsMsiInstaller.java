package com.mayak.iet.infrastructure.update.installer;

import com.mayak.iet.infrastructure.update.UpdatePaths;
import com.mayak.iet.infrastructure.util.OsUtils;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
public class WindowsMsiInstaller implements UpdateInstaller {

    @Override
    public void install(Path msiFile) throws Exception {
        if (!OsUtils.isWindows()) return;

        if (!Files.exists(msiFile)) {
            throw new IllegalArgumentException("MSI file not found: " + msiFile);
        }

        Path updaterExe = UpdatePaths.updaterExe();
        if (!Files.exists(updaterExe)) {
            throw new IllegalStateException("Updater EXE not found: " + updaterExe);
        }

        Path logFile = UpdatePaths.logsDir().resolve("installer-launch.log");
        Files.createDirectories(logFile.getParent());

        String now = java.time.LocalDateTime.now().toString();

        Files.writeString(
                logFile,
                "[" + now + "] Launching updater EXE: " + updaterExe + System.lineSeparator()
                        + "[" + now + "] MSI: " + msiFile + System.lineSeparator(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        ProcessBuilder pb = new ProcessBuilder(updaterExe.toString(), msiFile.toAbsolutePath().toString());

        pb.directory(updaterExe.getParent().toFile());
        pb.redirectErrorStream(true);
        pb.start();

        System.exit(0);
    }
}