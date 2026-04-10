package com.mayak.ietms.support.validation;

import com.mayak.ietms.common.validation.ValidationError;
import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.common.validation.Validator;
import com.mayak.ietms.ui.workspace.planner.controller.PlannerController;

public class TransportOrderValidator implements Validator<PlannerController> {
    @Override
    public ValidationResult isValid(PlannerController controller) {
        var result = new ValidationResult();

        String transportOrderText = controller.getTransportOrder().getText();
        boolean hasTransportOrder = transportOrderText != null && !transportOrderText.isBlank();

        if (hasTransportOrder) {
            String carrier = controller.getCarrierField().getText();
            if (carrier == null || carrier.isBlank()) {
                result.add(new ValidationError("carrier", "Carrier is required when transport order is set"));
            }

            String licensePlate = controller.getLicensePlateField().getText();
            if (licensePlate == null || licensePlate.isBlank()) {
                result.add(new ValidationError("licensePlate", "License plate is required when transport order is set"));
            }
        }

        return result;
    }
}