package com.mayak.ietms.features.license.api;

import com.mayak.ietms.features.license.application.LicenseQueryService;
import com.mayak.ietms.license.dto.LicenseResponseDto;
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

    /**
     * Returns the currently active license details.
     *
     * @return active license info
     * @throws com.mayak.ietms.shared.exception.business.LicenseException if no active license is found
     */
    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_LICENSE')")
    public LicenseResponseDto getCurrent() {
        return licenseQueryService.getCurrent();
    }

}