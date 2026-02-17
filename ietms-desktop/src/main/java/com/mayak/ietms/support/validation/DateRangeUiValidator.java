package com.mayak.ietms.support.validation;

import com.mayak.ietms.common.validation.ValidationError;
import com.mayak.ietms.common.validation.Validator;
import com.mayak.ietms.common.validation.ValidationResult;

public class DateRangeUiValidator implements Validator<DateRange> {

    @Override
    public ValidationResult isValid(DateRange object) {
        var result = new ValidationResult();

        var start = object.startPicker().getValue();
        var end = object.endPicker().getValue();

        if (start == null) {
            result.add(new ValidationError("startDate", "Start date is required"));
        }

        if (end == null) {
            result.add(new ValidationError("endDate", "End date is required"));
        }
        return  result;
    }
}