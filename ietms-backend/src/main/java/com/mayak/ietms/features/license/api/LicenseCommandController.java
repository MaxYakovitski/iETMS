package com.mayak.ietms.features.license.api;

import com.mayak.ietms.features.license.application.LicenseCommandService;
import com.mayak.ietms.license.dto.LicenseActivateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/license")
@Tag(name = "License", description = "License management")
@RequiredArgsConstructor
public class LicenseCommandController {

    private final LicenseCommandService licenseCommandService;

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_LICENSE')")
    @Operation(summary = "Activate license",
               description = "If another license is currently active, it will be deactivated first.")
    public void activate(@RequestBody LicenseActivateDto dto) {
        licenseCommandService.activate(dto.licenseKey());
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('MANAGE_LICENSE')")
    @Operation(summary = "Deactivate license", description = "Has no effect if no license is currently active.")
    public void deactivate() {
        licenseCommandService.deactivate();
    }
}