package com.mayak.ietms.features.license.application;

import com.mayak.ietms.features.license.domain.model.License;
import com.mayak.ietms.features.license.domain.model.LicenseInfo;
import com.mayak.ietms.features.license.infra.persistence.LicenseRepository;
import com.mayak.ietms.license.dto.LicenseResponseDto;
import com.mayak.ietms.shared.exception.business.LicenseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides read access to the current active license.
 */
@Service
@RequiredArgsConstructor
public class LicenseQueryService {

    private final LicenseRepository licenseRepository;
    private final LicenseVerifier licenseVerifier;

    /**
     * Returns the current active license details as a response DTO.
     *
     * @throws LicenseException if no active license is found
     */
    @Transactional(readOnly = true)
    public LicenseResponseDto getCurrent() {
        License license = licenseRepository.findByActiveTrue()
                .orElseThrow(() -> new LicenseException("No active license found!"));
        LicenseInfo info = licenseVerifier.verify(license.getLicenseKey());
        return new LicenseResponseDto(
                license.getId(),
                info.company(),
                info.maxUsers(),
                info.expiresAt()
        );
    }

    /**
     * Returns the maximum number of users allowed by the active license.
     *
     * @throws LicenseException if no active license is found
     */
    @Transactional(readOnly = true)
    public int getMaxUsers() {
        return getActiveLicenseInfo().maxUsers();
    }

    /**
     * Returns parsed info from the currently active license.
     *
     * @throws LicenseException if no active license is found
     */
    @Transactional(readOnly = true)
    public LicenseInfo getActiveLicenseInfo() {
        return licenseRepository.findByActiveTrue()
                .map(license -> licenseVerifier.verify(license.getLicenseKey()))
                .orElseThrow(() -> new LicenseException("No active license found!"));
    }
}