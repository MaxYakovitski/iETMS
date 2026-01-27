package com.mayak.iet.infrastructure.update;

public record UpdateCheckResult (
        boolean updateRequired,
        boolean forced,
        String currentVersion,
        String latestVersion) {
}