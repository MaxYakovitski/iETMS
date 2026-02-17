package com.mayak.iet.support.validation;

import com.mayak.iet.ui.workspace.planner.controller.PlannerController;
import com.mayak.iet.common.validation.ValidationError;
import com.mayak.iet.common.validation.ValidationResult;
import com.mayak.iet.common.validation.Validator;

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
