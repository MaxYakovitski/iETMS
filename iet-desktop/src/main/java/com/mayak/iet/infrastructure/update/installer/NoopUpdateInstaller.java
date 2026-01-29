package com.mayak.iet.infrastructure.update.installer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Slf4j
@Service
@Profile("!windows")
public class NoopUpdateInstaller implements UpdateInstaller {

    @Override
    public void install(Path file) {
        log.info("[UPDATE] noop installer, skipping install for {}", file);
    }
}