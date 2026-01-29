package com.mayak.iet.infrastructure.update.installer;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@Profile("windows")
public class WindowsMsiInstaller implements UpdateInstaller {

    @Override
    public void install(Path msiFile) throws Exception {
        new ProcessBuilder("msiexec", "/i", msiFile.toAbsolutePath().toString(), "/qn").start();
    }
}