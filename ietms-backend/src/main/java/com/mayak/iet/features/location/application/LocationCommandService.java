package com.mayak.iet.features.location.application;

import com.mayak.iet.location.dto.LocationDto;
import com.mayak.iet.features.location.domain.model.Location;
import com.mayak.iet.shared.exception.business.LocationInUseException;
import com.mayak.iet.shared.exception.business.LocationNotFoundException;
import com.mayak.iet.shared.exception.validation.ValidationException;
import com.mayak.iet.features.location.infra.mapping.LocationMapper;
import com.mayak.iet.features.location.infra.persistence.LocationRepository;
import com.mayak.iet.features.request.infra.persistence.RequestRepository;
import com.mayak.iet.location.validator.LocationContractValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationCommandService {

    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final LocationMapper locationMapper;
    private final LocationContractValidator locationContractValidator;

    @Transactional
    public Location add(LocationDto dto) {
        validate(dto);

        Location location = locationMapper.toEntity(dto);
        Location saved = locationRepository.save(location);

        log.info("Location placed with ID: {}", saved.getId());
        return saved;
    }

    @Transactional
    public Location resolve(Location location) {
        if (location == null) return null;

        location.normalizeForSearch();

        return locationRepository
                .findByCountryCodeAndZipCodeAndPlaceName(
                        location.getCountryCode(),
                        location.getZipCode(),
                        location.getPlaceName()
                )
                .orElseGet(() -> locationRepository.save(location));
    }

    // --- UPDATE ---
    @Transactional
    public void update(LocationDto dto) {
        validate(dto);

        Location location = getOrThrow(dto.id());
        locationMapper.updateEntityFromDto(dto, location);

        locationRepository.save(location);
        log.info("Location updated with ID: {}", location.getId());
    }

    // --- DELETE ---
    @Transactional
    public void delete(Long id) {
        Location location = getOrThrow(id);
        String json = "[" + id + "]";

        if (requestRepository.existsByLocationUsed(json)) {
            throw new LocationInUseException(id);
        }

        locationRepository.delete(location);
        log.info("Location {} deleted", id);
    }

    // --- HELPERS ---
    private void validate(LocationDto dto) {
        var result = locationContractValidator.isValid(dto);
        if (!result.isValid()) throw new ValidationException(result);
    }

    private Location getOrThrow(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new LocationNotFoundException(locationId));
    }
}
