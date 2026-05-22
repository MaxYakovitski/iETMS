package com.mayak.ietms.features.location.application;

import com.mayak.ietms.location.dto.LocationDto;
import com.mayak.ietms.features.location.domain.model.Location;
import com.mayak.ietms.features.location.infra.mapping.LocationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class LocationResolver {

    private final LocationQueryService locationQueryService;
    private final LocationMapper locationMapper;

    /**
     * Loads all locations by ID and returns them as a map for efficient batch lookup.
     *
     * @param locationIds set of location IDs to resolve
     * @return map of location ID to its DTO; empty map if input is null or empty
     */
    @Transactional(readOnly = true)
    public Map<Long, LocationDto> resolveAsMap(Set<Long> locationIds) {
        if (locationIds == null || locationIds.isEmpty()) return Map.of();
        return locationQueryService.loadLocations(new ArrayList<>(locationIds))
                .stream()
                .collect(Collectors.toMap(Location::getId, locationMapper::toDto));
    }

    public record ResolvedLocations(List<LocationDto> from, List<LocationDto> to) {}

    /**
     * Resolves from/to location lists in a single database query.
     *
     * @param fromIds list of origin location IDs (may be null)
     * @param toIds   list of destination location IDs (may be null)
     * @return resolved from/to location lists preserving original order
     */
    @Transactional(readOnly = true)
    public ResolvedLocations resolve(List<Long> fromIds, List<Long> toIds) {
        Set<Long> allIds = Stream.concat(
                fromIds != null ? fromIds.stream() : Stream.empty(),
                toIds   != null ? toIds.stream()   : Stream.empty()
        ).collect(Collectors.toSet());

        Map<Long, LocationDto> cache = resolveAsMap(allIds);
        List<LocationDto> from = fromIds == null ? List.of() : fromIds.stream().map(cache::get).filter(Objects::nonNull).toList();
        List<LocationDto> to   = toIds   == null ? List.of() : toIds.stream().map(cache::get).filter(Objects::nonNull).toList();
        return new ResolvedLocations(from, to);
    }
}