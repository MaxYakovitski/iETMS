package com.mayak.ietms.location.util;

import com.mayak.ietms.common.util.UnicodeNormalizer;
import com.mayak.ietms.location.dto.LocationDto;
import com.mayak.ietms.common.validation.ValidationResult;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Locale;

@NoArgsConstructor
public class LocationParser {

    public static final String REGEX = "[^a-zA-Z0-9]+";
    private static final String LINE_REGEX = "^[A-Z]{2}[^A-Z0-9]+[A-Z0-9]{2,7}([^A-Z0-9]+[A-Z.\\-'\\s]*)?$";
    public static final String ZIP_REGEX = "[A-Z0-9]{2,7}";

    public static ValidationResult validateLine(String line) {
        ValidationResult result = new ValidationResult();

        if (line == null || line.isBlank()) {
            result.add("location", "Line is empty");
            return result;
        }

        String normalized = sanitize(line);

        if (!normalized.matches(LINE_REGEX)) {
            result.add("location", "Invalid location format");
            return result;
        }

        String[] parts = normalized.split(REGEX, 3);

        if (parts.length < 2) {
            result.add("location", "Not enough parts");
            return result;
        }

        CountryZipPattern.of(parts[0]).ifPresentOrElse(
                p -> {
                    if (!p.matches(parts[1]) && !p.matchesPartial(parts[1]))
                        result.add("zipCode", "Invalid zip code for " + parts[0]);
                },
                () -> {
                    if (!Arrays.asList(Locale.getISOCountries()).contains(parts[0]))
                        result.add("countryCode", "Invalid country code");
                    else if (!parts[1].matches(ZIP_REGEX))
                        result.add("zipCode", "Invalid zip code");
                }
        );

        return result;
    }

    public static LocationDto parse(String line) {
        ValidationResult vr = validateLine(line);
        if (!vr.isValid()) return null;

        String[] parts = sanitize(line).split(REGEX, 3);

        return new LocationDto(
                null,
                parts[0],
                parts[1],
                parts.length > 2 ? parts[2] : null
        );
    }

    private static String sanitize(String line) {
        return UnicodeNormalizer.toAscii(line).toUpperCase().trim();
    }
}