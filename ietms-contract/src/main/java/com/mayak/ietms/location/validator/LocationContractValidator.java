package com.mayak.ietms.location.validator;

import com.mayak.ietms.location.dto.LocationDto;
import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.common.validation.Validator;
import com.mayak.ietms.location.util.CountryZipPattern;

import java.util.Arrays;
import java.util.Locale;

import static com.mayak.ietms.location.util.LocationParser.ZIP_REGEX;

public class LocationContractValidator implements Validator<LocationDto> {

    @Override
    public ValidationResult isValid(LocationDto object) {
        ValidationResult validationResult = new ValidationResult();

        if (object == null) {
            validationResult.add("location", "Location is missing");
            return validationResult;
        }

        String code = object.countryCode() != null ? object.countryCode().toUpperCase() : "";

        if (code.isEmpty() || !Arrays.asList(Locale.getISOCountries()).contains(object.countryCode().toUpperCase())) {
            validationResult.add("countryCode", "Invalid country code");
        }

        if (object.zipCode() == null || !object.zipCode().matches(ZIP_REGEX)) {
            validationResult.add("zipCode", "Invalid zip code");
        } else {
            CountryZipPattern.of(code).ifPresentOrElse(
                    p -> {
                        if (!p.matches(object.zipCode()) && !p.matchesPartial(object.zipCode()))
                            validationResult.add("zipCode", "Invalid zip code");
                    },
                    () -> {
                        if (!object.zipCode().matches(ZIP_REGEX))
                            validationResult.add("zipCode", "Invalid zip code");
                    }
            );
        }

        if (object.placeName() == null || object.placeName().isBlank()) {
            validationResult.add("placeName", "Place name required");
        }
        return validationResult;
    }
}