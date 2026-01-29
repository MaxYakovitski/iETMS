package com.mayak.iet.infrastructure.update;

import com.mayak.iet.infrastructure.update.installer.UpdateInstaller;
import com.mayak.iet.infrastructure.version.AppVersionProvider;
import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
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
            @Qualifier("updateRestTemplate") RestTemplate restTemplate,
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

        new Thread(() -> {
            try {
                Files.createDirectories(targetFile.getParent());
                URL url = URI.create(result.downloadUrl()).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                long totalBytes = result.downloadSize();
                long downloaded = 0;

                listener.onStart(result.currentVersion(), result.latestVersion());

                try (
                        InputStream in = conn.getInputStream();
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

                setState(UpdateState.VERIFYING);

                if (result.expectedSha256() != null && !result.expectedSha256().isBlank()) {
                    String actualSha256 = ChecksumUtils.sha256(targetFile);
                    if (!actualSha256.equalsIgnoreCase(result.expectedSha256())) {
                        throw new IllegalStateException(
                                "Checksum mismatch: expected=" + result.expectedSha256()
                                        + ", actual=" + actualSha256
                        );
                    }
                }

                setState(UpdateState.INSTALLING);
                installer.install(targetFile);

                Platform.exit();
                System.exit(0);

            } catch (Exception e) {
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