package com.mayak.iet.infrastructure.update;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Bootstrap update service.
 * Current implementation simulates update flow.
 * Real implementation will:
 *  - fetch manifest.json
 *  - download archive
 *  - verify checksum
 *  - replace application
 */

@Service
@Slf4j
public class UpdateService {

    public UpdateCheckResult checkVersion() {
        // TODO: later → fetch from backend / manifest.json
        log.info("[UPDATE] checking version");
        String current = "1.3.0";
        String latest = "1.4.0";

        boolean updateRequired = true;
        boolean forced = true;

        return new UpdateCheckResult(updateRequired, forced, current, latest);
    }

    public void downloadUpdate(UpdateCheckResult result, UpdateListener listener) {
        log.info("[UPDATE] starting download {} -> {}", result.currentVersion(), result.latestVersion());

        new Thread(() -> {
            try {
                listener.onStart(result.currentVersion(), result.latestVersion());

                for (int i = 1; i <= 100; i++) {
                    Thread.sleep(40);
                    listener.onProgress(i / 100.0);
                    listener.onMessage("downloading update… " + i + "%");
                }

                listener.onMessage("finalizing update…");
                Thread.sleep(600);

                listener.onComplete();

            } catch (Exception e) {
                listener.onError(e);
            }
        }, "update-downloader").start();
    }
}