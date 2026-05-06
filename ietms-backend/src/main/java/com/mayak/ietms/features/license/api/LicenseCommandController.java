package com.mayak.ietms.features.license.api;

import com.mayak.ietms.features.license.application.LicenseCommandService;
import com.mayak.ietms.license.dto.LicenseActivateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Handles license activation and deactivation.
 */
@RestController
@RequestMapping("/api/license")
@RequiredArgsConstructor
public class LicenseCommandController {

    private final LicenseCommandService licenseCommandService;

    /**
     * Activates a new license key.
     * If another license is currently active, it will be deactivated first.
     *
     * @param dto DTO containing the license key to activate
     */
    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_LICENSE')")
    public void activate(@RequestBody LicenseActivateDto dto) {
        licenseCommandService.activate(dto.licenseKey());
    }

    /**
     * Deactivates the currently active license.
     * Has no effect if no license is active.
     */
    @DeleteMapping
    @PreAuthorize("hasAuthority('MANAGE_LICENSE')")
    public void deactivate() {
        licenseCommandService.deactivate();
    }
}