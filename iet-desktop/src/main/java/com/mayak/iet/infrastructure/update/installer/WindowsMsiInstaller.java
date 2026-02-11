package com.mayak.iet.infrastructure.update.installer;

import com.mayak.iet.infrastructure.update.UpdatePaths;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Profile("windows")
public class WindowsMsiInstaller implements UpdateInstaller {

    @Override
    public void install(Path msiFile) throws Exception {

        Path javaExe = UpdatePaths.installDir()
                .resolve("runtime")
                .resolve("bin")
                .resolve("java.exe");

        Path updaterJar = UpdatePaths.updaterJar();

        Path logFile = UpdatePaths.updatesDir().resolve("installer-launch.log");
        Files.createDirectories(logFile.getParent());

        try {
            Files.writeString(logFile,
                    "JAVA: " + javaExe + "\n" +
                            "UPDATER: " + updaterJar + "\n" +
                            "MSI: " + msiFile + "\n",
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
            );

            ProcessBuilder pb = new ProcessBuilder(
                    javaExe.toString(),
                    "-jar",
                    updaterJar.toString(),
                    msiFile.toString()
            );

            pb.redirectOutput(logFile.toFile());
            pb.redirectErrorStream(true);

            pb.start();

        } catch (IOException e) {
            Files.writeString(logFile,
                    "\nERROR: " + e.getMessage(),
                    java.nio.file.StandardOpenOption.APPEND);
            throw e;
        }

        System.exit(0);
    }
}