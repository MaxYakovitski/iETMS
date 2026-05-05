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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public void activate(@RequestBody LicenseActivateDto dto) {
        licenseCommandService.activate(dto.licenseKey());
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivate(@PathVariable("id") Long id) {
        licenseCommandService.deactivate(id);
    }
}