package com.mayak.ietms.features.license.api;

import com.mayak.ietms.features.license.application.LicenseQueryService;
import com.mayak.ietms.license.dto.LicenseResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/license")
@Tag(name = "License", description = "License management")
@RequiredArgsConstructor
public class LicenseQueryController {

    private final LicenseQueryService licenseQueryService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_LICENSE')")
    @Operation(summary = "Get current license", description = "Returns 404 if no active license is found.")
    public LicenseResponseDto getCurrent() {
        return licenseQueryService.getCurrent();
    }

}