package com.mayak.iet.infrastructure.update.installer;

import com.mayak.iet.infrastructure.update.UpdatePaths;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

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

        new ProcessBuilder(
                javaExe.toString(),
                "-jar",
                updaterJar.toString(),
                msiFile.toString()
        ).start();

        System.exit(0);
    }
}