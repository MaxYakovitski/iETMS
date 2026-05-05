package com.mayak.ietms.features.license.domain.model;

import java.time.LocalDate;

/**
 * Parsed payload extracted from a verified RSA-signed license key.
 */
public record LicenseInfo(
        String company,
        int maxUsers,
        LocalDate expiresAt) {
}