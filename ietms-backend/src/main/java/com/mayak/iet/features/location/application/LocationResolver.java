package com.mayak.iet.features.location.application;

import com.mayak.iet.location.dto.LocationDto;
import com.mayak.iet.features.location.domain.model.Location;
import com.mayak.iet.features.location.infra.mapping.LocationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationResolver {

    private final LocationQueryService locationQueryService;
    private final LocationMapper locationMapper;

    @Transactional(readOnly = true)
    public List<LocationDto> resolve(List<Long> locationIds) {
        if (locationIds == null || locationIds.isEmpty()) {
            return List.of();
        }

        List<Location> locations =
                locationQueryService.loadLocations(locationIds);

        Map<Long, Location> byId = locations.stream()
                .collect(Collectors.toMap(Location::getId, Function.identity()));

        return locationIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(locationMapper::toDto)
                .toList();
    }
}
