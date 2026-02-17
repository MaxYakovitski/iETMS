package com.mayak.ietms.infrastructure.update;

import java.time.Instant;

public record ManifestDto(
        String app,
        String platform,
        String latestVersion,
        boolean mandatory,
        Instant releaseDate,
        Download download,
        String notes) {

    public record Download(String type, String url, String sha256, long size) {
    }
}
