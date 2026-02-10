package com.mayak.iet.infrastructure.update;

import com.mayak.iet.infrastructure.update.installer.UpdateInstaller;
import com.mayak.iet.infrastructure.util.OsUtils;
import com.mayak.iet.infrastructure.version.AppVersionProvider;
import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

@Service
@Slf4j
public class UpdateService {

    private static final String MANIFEST_URL = "http://165.22.92.183/updates/windows/manifest.json";

    private final RestTemplate restTemplate;
    private final AppVersionProvider versionProvider;
    private final UpdateInstaller installer;

    @Setter
    private UpdateListener listener;
    @Getter
    private UpdateState state = UpdateState.IDLE;

    public UpdateService(
            RestTemplate restTemplate,
            AppVersionProvider versionProvider,
            UpdateInstaller installer) {

        this.restTemplate = restTemplate;
        this.versionProvider = versionProvider;
        this.installer = installer;
    }

    public UpdateCheckResult checkVersion() {
        if (!OsUtils.isWindows()) {
            log.info("[UPDATE] disabled on non-Windows OS");
            return UpdateCheckResult.noUpdate(versionProvider.getAppVersion());
        }

        setState(UpdateState.CHECKING);

        try {
            ManifestDto manifest =
                    restTemplate.getForObject(MANIFEST_URL, ManifestDto.class);

            if (manifest == null) {
                return UpdateCheckResult.noUpdate(getCurrentVersion());
            }

            String current = getCurrentVersion();
            String latest = manifest.latestVersion();

            boolean updateRequired = !Objects.equals(current, latest);
            boolean forced = manifest.mandatory();

            log.info("[UPDATE] current={}, latest={}, mandatory={}", current, latest, forced);

            return new UpdateCheckResult(
                    updateRequired,
                    forced,
                    current,
                    latest,
                    manifest.download().url(),
                    manifest.download().size(),
                    manifest.download().sha256());

        } catch (Exception e) {
            log.error("[UPDATE] failed to fetch manifest", e);
            setState(UpdateState.FAILED);
            return UpdateCheckResult.noUpdate(getCurrentVersion());
        }
    }

    public void startMandatoryUpdate(UpdateCheckResult result) {
        setState(UpdateState.DOWNLOADING);

        Path targetFile = UpdatePaths.msiFile(result.latestVersion());

        new Thread(() -> {
            if (listener != null) {
                listener.onStart(result.currentVersion(), result.latestVersion());
            }

            try {
                Files.createDirectories(targetFile.getParent());

                HttpClient client = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.ALWAYS)
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(result.downloadUrl()))
                        .GET()
                        .build();

                HttpResponse<InputStream> response =
                        client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() != 200) {
                    throw new IllegalStateException("Download failed, HTTP status=" + response.statusCode());
                }

                long total = result.downloadSize();
                long downloaded = 0;

                byte[] buffer = new byte[8192];

                try (InputStream in = response.body();
                     OutputStream out = Files.newOutputStream(
                             targetFile,
                             StandardOpenOption.CREATE,
                             StandardOpenOption.TRUNCATE_EXISTING
                     )) {

                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                        downloaded += read;

                        if (listener != null && total > 0) {
                            double progress = (double) downloaded / total;

                            double downloadedMb = downloaded / 1024d / 1024d;
                            double totalMb = total / 1024d / 1024d;

                            listener.onProgress(progress);
                            listener.onMessage(
                                    String.format(
                                            "Downloading update… %.2f / %.2f MB (%d%%)",
                                            downloadedMb,
                                            totalMb,
                                            (int) (progress * 100)
                                    )
                            );
                        }
                    }
                }

                log.info("[UPDATE] download completed, size={}", Files.size(targetFile));

                setState(UpdateState.VERIFYING);

                if (result.expectedSha256() != null && !result.expectedSha256().isBlank()) {
                    String actual = ChecksumUtils.sha256(targetFile);
                    if (!actual.equalsIgnoreCase(result.expectedSha256())) {
                        throw new IllegalStateException(
                                "Checksum mismatch: expected=" + result.expectedSha256()
                                        + ", actual=" + actual
                        );
                    }
                }

                if (!OsUtils.isWindows()) {
                    log.warn("[UPDATE] DEV mode: installer skipped (non-Windows)");
                    return;
                }

                setState(UpdateState.INSTALLING);
                installer.install(targetFile);

                Platform.exit();
                System.exit(0);

            } catch (Exception e) {
                log.error("[UPDATE] mandatory update failed", e);
                setState(UpdateState.FAILED);
                if (listener != null) listener.onError(e);
            }
        }, "update-worker").start();
    }

    private void setState(UpdateState newState) {
        this.state = newState;
        log.info("[UPDATE] state = {}", newState);
    }

    private String getCurrentVersion() {
        return versionProvider.getAppVersion();
    }
}