package com.mayak.ietms.features.license.infra.persistence;

import com.mayak.ietms.features.license.domain.model.License;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Persistence repository for {@link License} entities.
 */
public interface LicenseRepository extends JpaRepository<License, Long> {

    /** Returns the currently active license if present. */
    Optional<License> findByActiveTrue();

    /** Returns a license by its key, regardless of active status. */
    Optional<License> findByLicenseKey(String licenseKey);
}