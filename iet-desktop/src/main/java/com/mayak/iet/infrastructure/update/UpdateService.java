package com.mayak.iet.infrastructure.update;

import com.mayak.iet.infrastructure.update.installer.UpdateInstaller;
import com.mayak.iet.infrastructure.version.AppVersionProvider;
import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
@Slf4j
public class UpdateService {

    private static final String MANIFEST_URL =
            "https://maxyakovitski.github.io/iETMS-updates/windows/manifest.json";


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
        setState(UpdateState.CHECKING);

        try {
            ManifestDto manifest =
                    restTemplate.getForObject(MANIFEST_URL, ManifestDto.class);

            if (manifest == null) {
                return UpdateCheckResult.noUpdate(getCurrentVersion());
            }

            String current = getCurrentVersion();
            String latest = manifest.latestVersion();

            boolean updateRequired = !current.equals(latest);
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
        log.info("[UPDATE] mandatory update started");
        log.info("[UPDATE] currentVersion={}", result.currentVersion());
        log.info("[UPDATE] targetVersion={}", result.latestVersion());
        log.info("[UPDATE] downloadUrl={}", result.downloadUrl());
        log.info("[UPDATE] targetFile={}", targetFile.toAbsolutePath());
        log.info("[UPDATE] expectedSize={}", result.downloadSize());
        log.info("[UPDATE] expectedSha256={}", result.expectedSha256());

        log.info("[UPDATE] starting update-worker thread");
        new Thread(() -> {
            log.info("[UPDATE] update-worker thread started");
            try {

                long totalBytes = result.downloadSize();
                long downloaded = 0;

                log.info("[UPDATE] starting HTTP download");
                ResponseEntity<Resource> response =
                        restTemplate.exchange(result.downloadUrl(), HttpMethod.GET, HttpEntity.EMPTY, Resource.class);

                log.info("[UPDATE] HTTP status={}", response.getStatusCode());

                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new IllegalStateException(
                            "Update download failed, status=" + response.getStatusCode()
                    );
                }

                Resource resource = response.getBody();
                if (resource == null) {
                    log.error("[UPDATE] download response body is NULL");
                    throw new IllegalStateException("Empty update download response");
                }

                log.info("[UPDATE] resource class={}", resource.getClass().getName());
                log.info("[UPDATE] ensuring target directory exists: {}",
                        targetFile.getParent().toAbsolutePath());

                Files.createDirectories(targetFile.getParent());

                try (
                        InputStream in = resource.getInputStream();
                        OutputStream out = Files.newOutputStream(
                                targetFile,
                                StandardOpenOption.CREATE,
                                StandardOpenOption.TRUNCATE_EXISTING
                        )
                ) {
                    byte[] buffer = new byte[8192];
                    int read;

                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                        downloaded += read;

                        double progress =
                                totalBytes > 0 ? (double) downloaded / totalBytes : 0.0;

                        listener.onProgress(progress);
                        listener.onMessage("downloading update… " + (int) (progress * 100) + "%");
                    }
                }

                log.info("[UPDATE] download completed, bytesDownloaded={}", downloaded);
                log.info("[UPDATE] file size on disk={}", Files.size(targetFile));

                setState(UpdateState.VERIFYING);

                if (result.expectedSha256() != null && !result.expectedSha256().isBlank()) {
                    String actualSha256 = ChecksumUtils.sha256(targetFile);
                    log.info("[UPDATE] actualSha256={}", actualSha256);
                    if (!actualSha256.equalsIgnoreCase(result.expectedSha256())) {
                        throw new IllegalStateException(
                                "Checksum mismatch: expected=" + result.expectedSha256()
                                        + ", actual=" + actualSha256
                        );
                    }
                }

                setState(UpdateState.INSTALLING);
                log.info("[UPDATE] launching MSI installer: {}", targetFile.toAbsolutePath());
                installer.install(targetFile);

                Platform.exit();
                System.exit(0);

            } catch (Exception e) {
                log.error("[UPDATE] mandatory update failed", e);
                setState(UpdateState.FAILED);
                listener.onError(e);
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