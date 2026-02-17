package com.mayak.ietms.support.validation;

import com.mayak.ietms.ui.workspace.planner.controller.PlannerController;
import com.mayak.ietms.common.validation.ValidationError;
import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.common.validation.Validator;

public class StatusDateTimeValidator implements Validator <PlannerController> {

    @Override
    public ValidationResult isValid(PlannerController object) {
        var result = new ValidationResult();

        if (object.getDateAndTime().getValue() == null) {
            result.add(new ValidationError("statusDate", "Date is required"));
        }

        if (object.getTimeSpinner().getValue() == null) {
            result.add(new ValidationError("statusTime", "Time is required"));
        }

        return result;
    }
}
