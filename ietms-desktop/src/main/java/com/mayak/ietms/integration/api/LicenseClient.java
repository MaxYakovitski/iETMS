package com.mayak.ietms.integration.api;

import com.mayak.ietms.license.dto.LicenseActivateDto;
import com.mayak.ietms.license.dto.LicenseResponseDto;

import java.util.Optional;

public interface LicenseClient {

    /**
     * Returns the currently active license details,
     * or {@link Optional#empty()} if no active license is found.
     */
    Optional<LicenseResponseDto> getCurrent();

    /**
     * Activates a new license using the provided key.
     *
     * @param dto DTO containing the license key to activate
     */
    void activate(LicenseActivateDto dto);

    /**
     * Deactivates the currently active license.
     */
    void deactivate();
}