package com.mayak.iet.common.validation;

import static com.mayak.iet.common.domain.CargoConstraints.*;

import com.mayak.iet.location.dto.LocationDto;
import com.mayak.iet.location.util.LocationParser;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class ValidationUtils {

    public static boolean isValidLocation(String raw) {
        if (raw == null || raw.isBlank()) return false;
        return LocationParser.validateLine(raw).isValid();
    }

    public static boolean isValidLocation(LocationDto location) {
        if (location == null) return false;
        if (location.countryCode() == null || location.countryCode().isBlank()) return false;
        if (location.zipCode() == null || location.zipCode().isBlank()) return false;
        return true;
    }

    public static boolean isValidWeight(Double weight) {
        return weight != null && weight > 0 && weight <= MAX_LOAD_CAPACITY;
    }

    public static boolean isValidTemperature(String text) {
        if (text == null || text.isBlank()) return false;
        String normalized = text.trim().replaceAll("\\s+", " ");
        return normalized.matches("^[+-]\\d{1,2}\\s*[+-]\\d{1,2}$");
    }

    public static boolean isValidLoadingMeter(Double loadingMeter) {
        return loadingMeter != null && loadingMeter > 0 && loadingMeter <= MAX_LOADING_METER_LTL;
    }
}