package com.mayak.ietms.features.license.application;

import com.mayak.ietms.features.license.domain.model.License;
import com.mayak.ietms.features.license.domain.model.LicenseInfo;
import com.mayak.ietms.features.license.infra.persistence.LicenseRepository;
import com.mayak.ietms.shared.exception.business.LicenseException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LicenseCommandService {

    private final LicenseRepository licenseRepository;
    private final LicenseVerifier licenseVerifier;

    /**
     * Activates a new license key.
     * Deactivates any previously active license before saving the new one.
     *
     * @throws LicenseException if the key is invalid or tampered
     */
    @Transactional
    public void activate(String licenseKey) {
        LicenseInfo info = verifyOrThrow(licenseKey);

        licenseRepository.findByActiveTrue()
                .ifPresent(existing -> {
                    existing.setActive(false);
                    licenseRepository.save(existing);
                });

        License license = License.builder()
                .licenseKey(licenseKey)
                .active(true)
                .build();

        licenseRepository.save(license);
        log.info("License activated: company={}, maxUsers={}, expiresAt={}",
                info.company(), info.maxUsers(), info.expiresAt());
    }

    /**
     * Deactivates the currently active license.
     */
    @Transactional
    public void deactivate(Long id) {
        License license = licenseRepository.findById(id)
                .orElseThrow(() -> new LicenseException("License not found: " + id));
        license.setActive(false);
        log.info("License deactivated: id={}", id);
    }

    private LicenseInfo verifyOrThrow(String licenseKey) {
        try {
            return licenseVerifier.verify(licenseKey);
        } catch (JwtException e) {
            throw new LicenseException("Invalid license key: " + e.getMessage());
        }
    }

}