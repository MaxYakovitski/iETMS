package com.mayak.iet.infrastructure.update;

public record UpdateCheckResult (
        boolean updateRequired,
        boolean forced,
        String currentVersion,
        String latestVersion,
        String downloadUrl,
        long downloadSize,
        String expectedSha256) {

    public static UpdateCheckResult noUpdate(String currentVersion) {
        return new UpdateCheckResult(
                false,
                false,
                currentVersion,
                currentVersion,
                null,
                0,
                null
        );
    }
}