package com.mayak.iet.common.util.formatting;

import com.mayak.iet.location.dto.LocationDto;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor
public class LocationTextFormatter {

    public static String format(LocationDto loc) {
        return Stream.of(
                        loc.countryCode(),
                        loc.zipCode(),
                        loc.placeName()
                )
                .filter(v -> v != null && !v.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(", "));
    }

    public static String formatAll(List<LocationDto> locations) {
        if (locations == null || locations.isEmpty()) return "";

        return locations.stream()
                .map(LocationTextFormatter::format)
                .collect(Collectors.joining(" + "));
    }
}