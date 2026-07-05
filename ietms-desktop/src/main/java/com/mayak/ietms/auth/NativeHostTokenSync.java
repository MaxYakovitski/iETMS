package com.mayak.ietms.auth;

import com.mayak.ietms.auth.event.SessionClearedEvent;
import com.mayak.ietms.auth.event.TokenChangedEvent;
import com.mayak.ietms.infrastructure.update.AppPaths;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author ma_yak
 */

@Component
@Slf4j
public class NativeHostTokenSync {

    @EventListener
    public void onTokenChanged(TokenChangedEvent event) {
        writeFile(AppPaths.accessTokenFile(), event.accessToken());
        writeFile(AppPaths.refreshTokenFile(), event.refreshToken());
    }

    @EventListener(SessionClearedEvent.class)
    public void onSessionCleared() {
        deleteFile(AppPaths.accessTokenFile());
        deleteFile(AppPaths.refreshTokenFile());
    }

    private void writeFile(Path path, String content) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("[auth] Failed to write token file {}", path, e);
        }
    }

    private void deleteFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {}
    }

}
