package com.mayak.iet.location.validator;

import com.mayak.iet.location.dto.LocationDto;
import com.mayak.iet.common.validation.ValidationResult;
import com.mayak.iet.common.validation.Validator;

import java.util.Arrays;
import java.util.Locale;

import static com.mayak.iet.location.util.LocationParser.ZIP_REGEX;

public class LocationContractValidator implements Validator<LocationDto> {

    @Override
    public ValidationResult isValid(LocationDto object) {
        ValidationResult validationResult = new ValidationResult();

        if (object == null) {
            validationResult.add("location", "Location is missing");
            return validationResult;
        }

        if (object.countryCode() == null || !Arrays.asList(Locale.getISOCountries()).contains(object.countryCode().toUpperCase())) {
            validationResult.add("countryCode", "Invalid country code");
        }

        if (object.zipCode() == null || !object.zipCode().matches(ZIP_REGEX)) {
            validationResult.add("zipCode", "Invalid zip code");
        }

        if (object.placeName() == null || object.placeName().isBlank()) {
            validationResult.add("placeName", "Place name required");
        }
        return validationResult;
    }
}