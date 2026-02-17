package com.mayak.ietms.common.util.formatting;

import com.mayak.ietms.common.util.TextSanitizer;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@NoArgsConstructor
public class LocationTextParser {

    public static List<String> parseLocations(String raw) {
        if (raw == null || raw.isBlank()) return null;

        return Arrays.stream(raw.split("\\+"))
                .map(TextSanitizer::safeTrim)
                .toList();
    }
}