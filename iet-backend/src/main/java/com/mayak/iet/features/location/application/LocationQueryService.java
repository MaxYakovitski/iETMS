package com.mayak.iet.features.location.application;

import com.mayak.iet.location.dto.LocationDto;
import com.mayak.iet.features.location.domain.model.Location;
import com.mayak.iet.features.location.infra.mapping.LocationMapper;
import com.mayak.iet.features.location.infra.persistence.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationQueryService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Transactional(readOnly = true)
    public List<LocationDto> findAll() {
        return locationRepository.findAll()
                .stream()
                .map(locationMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Location> loadLocations(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return locationRepository.findAllById(ids);
    }
}
