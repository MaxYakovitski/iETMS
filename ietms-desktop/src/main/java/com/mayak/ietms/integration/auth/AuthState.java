package com.mayak.ietms.integration.auth;

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
 * The token is stored both in memory (for use within the application)
 * and in {@code %LOCALAPPDATA%\iETMS\token} file —
 * to be read by the browser extension's native messaging host.
 */
@Component
@Slf4j
@Getter
public class AuthState {

    private String token;

    public boolean isAuthenticated() {
        return token != null;
    }

    public void setToken(String token) {
        this.token = token;
        writeTokenFile(token);
    }

    public void clear() {
        this.token = null;
        deleteTokenFile();
    }

    private Path tokenFilePath() {
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData == null) return null;
        return Path.of(localAppData, "iETMS", "token");
    }

    private void writeTokenFile(String token) {
        try {
            Path path = tokenFilePath();
            if (path == null) return;
            Files.createDirectories(path.getParent());
            Files.writeString(path, token, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("[auth] Failed to write token file", e);
        }
    }

    private void deleteTokenFile() {
        try {
            Path path = tokenFilePath();
            if (path != null) Files.deleteIfExists(path);
        } catch (IOException ignored) {}
    }
}