package com.mayak.ietms.license.dto;

import java.time.LocalDate;

/**
 * Response with current active license details.
 */
public record LicenseResponseDto(
        Long id,
        String company,
        int maxUsers,
        LocalDate expiresAt
) { }