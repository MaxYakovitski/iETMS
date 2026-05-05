package com.mayak.ietms.features.license.api;

import com.mayak.ietms.features.license.application.LicenseQueryService;
import com.mayak.ietms.features.license.domain.model.LicenseInfo;
import com.mayak.ietms.features.license.infra.persistence.LicenseRepository;
import com.mayak.ietms.license.dto.LicenseResponseDto;
import com.mayak.ietms.shared.exception.business.LicenseException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides read access to the current active license.
 */
@RestController
@RequestMapping("/api/license")
@RequiredArgsConstructor
public class LicenseQueryController {

    private final LicenseQueryService licenseQueryService;
    private final LicenseRepository licenseRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public LicenseResponseDto getCurrent() {
        return licenseRepository.findByActiveTrue()
                .map(license -> {
                    LicenseInfo info = licenseQueryService.getActiveLicenseInfo();
                    return new LicenseResponseDto(
                            license.getId(),
                            info.company(),
                            info.maxUsers(),
                            info.expiresAt()
                    );
                })
                .orElseThrow(() -> new LicenseException("No active license found"));
    }

}