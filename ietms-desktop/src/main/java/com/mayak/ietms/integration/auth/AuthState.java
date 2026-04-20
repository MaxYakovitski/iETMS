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
 * The token is stored both in memory (for use within the application)
 * and in {@code %LOCALAPPDATA%\iETMS\native-host\token} file —
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
        return AppPaths.tokenFile();
    }

    private void writeTokenFile(String token) {
        try {
            Path path = tokenFilePath();
            Files.createDirectories(path.getParent());
            Files.writeString(path, token, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("[auth] Failed to write token file", e);
        }
    }

    private void deleteTokenFile() {
        try {
            Path path = tokenFilePath();
            Files.deleteIfExists(path);
        } catch (IOException ignored) {}
    }
}