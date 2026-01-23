package com.mayak.iet.common.validation;

import com.mayak.iet.common.dto.date.DateRangeDto;

public class DateRangeContractValidator implements Validator<DateRangeDto> {

    @Override
    public ValidationResult isValid(DateRangeDto object) {
        var validationResult = new ValidationResult();

        if (object == null) {
            validationResult.add("dateRange", "Date range is missing");
            return validationResult;
        }

        if (object.startDate() == null) {
            validationResult.add("startDate", "Start date is required");
        }

        if (object.endDate() == null) {
            validationResult.add("endDate", "End date is required");
        }

        if (object.startDate() != null && object.endDate() != null
                && object.endDate().isBefore(object.startDate())) {
            validationResult.add("endDate", "End date must be after start date");
        }

        return validationResult;
    }
}