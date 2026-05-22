package com.mayak.ietms.integration.auth;

import com.mayak.ietms.infrastructure.update.AppPaths;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Holds the authentication state of the current user.
 * <p>
 * The access accessToken is stored in memory and persisted to
 * {@code %LOCALAPPDATA%\iETMS\native-host\accessToken} —
 * to be read by the browser extension's native messaging host.
 * <p>
 * The refresh accessToken is stored in memory and persisted to
 * {@code %LOCALAPPDATA%\iETMS\native-host\refresh-accessToken} —
 * used exclusively by the desktop application for accessToken rotation.
 */
@Component
@Slf4j
@Getter
public class AuthState {

    private String token;
    private String refreshToken;

    public boolean isAuthenticated() {
        return token != null;
    }

    public void setToken(String token) {
        this.token = token;
        writeFile(AppPaths.tokenFile(), token);
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        writeFile(AppPaths.refreshTokenFile(), refreshToken);
    }

    public void clear() {
        this.token = null;
        this.refreshToken = null;
        deleteFile(AppPaths.tokenFile());
        deleteFile(AppPaths.refreshTokenFile());
    }

    private void writeFile(Path path, String content) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("[auth] Failed to write accessToken file", e);
        }
    }

    private void deleteFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {}
    }
}