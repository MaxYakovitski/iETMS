package com.mayak.iet.features.location.infra.persistence;

import com.mayak.iet.features.location.domain.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByCountryCodeAndZipCodeAndPlaceName(String countryCode, String zipCode,
                                                               String placeName);
}