package com.mayak.ietms.integration.nativemessaging;

import com.mayak.ietms.infrastructure.update.AppPaths;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Registers the native messaging host manifest for the browser extension.
 * <p>
 * Writes the manifest JSON to {@code %LOCALAPPDATA%\iETMS\native-host\manifest.json}
 * and registers it in {@code HKCU\Software\Google\Chrome\NativeMessagingHosts}
 * so that Chrome can launch this application as a native messaging host.
 * Registration is per-user, ensuring accessToken isolation in multi-user environments.
 */
@Component
@Slf4j
public class NativeMessagingRegistrar {

    private static final String HOST_NAME = "com.mayak.ietms";

    @Value("${bridge.extension-id}")
    private String extensionId;

    @PostConstruct
    public void register() {

        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            return;
        }

        try {
            Path exePath = AppPaths.nativeHostExe();
            Path manifestPath = AppPaths.nativeHostManifest();

            String manifest = """
                {
                  "name": "%s",
                  "description": "iETMS Native Messaging Host",
                  "path": "%s",
                  "type": "stdio",
                  "allowed_origins": ["%s"]
                }
                """.formatted(
                    HOST_NAME,
                    exePath.toString().replace("\\", "\\\\"),
                    "chrome-extension://" + extensionId + "/"
            );

            Files.createDirectories(manifestPath.getParent());
            Files.writeString(manifestPath, manifest, StandardCharsets.UTF_8);

            String regKey = "HKCU\\Software\\Google\\Chrome\\NativeMessagingHosts\\" + HOST_NAME;
            new ProcessBuilder("reg", "add", regKey,
                    "/ve", "/t", "REG_SZ", "/d", manifestPath.toString(), "/f")
                    .inheritIO().start().waitFor();
        } catch (Exception e) {
            log.warn("[native-messaging] Registration failed", e);
        }
    }
}