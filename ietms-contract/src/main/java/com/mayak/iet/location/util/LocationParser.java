package com.mayak.iet.location.util;

import com.mayak.iet.location.dto.LocationDto;
import com.mayak.iet.common.validation.ValidationResult;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Locale;

@NoArgsConstructor
public class LocationParser {

    public static final String REGEX = "[^a-zA-Z0-9]+";
    private static final String LINE_REGEX = "^(?:(GB|IE|MT)[^A-Z0-9]+[A-Z0-9]{2,7}|([A-Z]{2})[^A-Z0-9]+[0-9]{2,7})([^A-Z0-9]+[A-Z.\\-'\\s]*)?$";
    public static final String ZIP_REGEX = "[A-Z0-9]{2,7}";

    public static ValidationResult validateLine(String line) {
        ValidationResult result = new ValidationResult();

        if (line == null || line.isBlank()) {
            result.add("location", "Line is empty");
            return result;
        }

        String normalized = line.toUpperCase().trim();

        if (!normalized.matches(LINE_REGEX)) {
            result.add("location", "Invalid location format");
            return result;
        }

        String[] parts = normalized.split(REGEX, 3);

        if (parts.length < 2) {
            result.add("location", "Not enough parts");
            return result;
        }

        if (!Arrays.asList(Locale.getISOCountries()).contains(parts[0])) {
            result.add("countryCode", "Invalid country code");
        }

        if (!parts[1].matches(ZIP_REGEX)) {
            result.add("zipCode", "Invalid zip code");
        }

        return result;
    }

    public static LocationDto parse(String line) {
        ValidationResult vr = validateLine(line);
        if (!vr.isValid()) return null;

        String[] parts = line.toUpperCase().trim().split(REGEX, 3);

        return new LocationDto(
                null,
                parts[0],
                parts[1],
                parts.length > 2 ? parts[2] : null
        );
    }
}