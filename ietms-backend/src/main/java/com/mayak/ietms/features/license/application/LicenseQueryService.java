package com.mayak.ietms.features.license.application;

import com.mayak.ietms.features.license.domain.model.LicenseInfo;
import com.mayak.ietms.features.license.infra.persistence.LicenseRepository;
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
     * Returns parsed info from the currently active license.
     *
     * @throws LicenseException if no active license is found
     */
    @Transactional(readOnly = true)
    public LicenseInfo getActiveLicenseInfo() {
        return licenseRepository.findByActiveTrue()
                .map(license -> licenseVerifier.verify(license.getLicenseKey()))
                .orElseThrow(() -> new LicenseException("No active license found"));
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
}