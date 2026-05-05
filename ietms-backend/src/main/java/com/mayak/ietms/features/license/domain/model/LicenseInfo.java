package com.mayak.ietms.features.license.domain.model;

import java.time.LocalDate;

public record LicenseInfo(
        String company,
        int maxUsers,
        LocalDate expiresAt) {
}