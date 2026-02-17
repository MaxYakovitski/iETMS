package com.mayak.iet.lane.validator;

import com.mayak.iet.lane.dto.LaneCreateDto;
import com.mayak.iet.request.dto.enums.TransportTypeDto;
import com.mayak.iet.common.validation.ValidationUtils;
import com.mayak.iet.common.validation.ValidationResult;
import com.mayak.iet.common.validation.Validator;

import static com.mayak.iet.common.domain.CargoConstraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LaneContractValidator implements Validator<LaneCreateDto> {

    @Override
    public ValidationResult isValid(LaneCreateDto object) {
        var validationResult = new ValidationResult();

        if (object == null) {
            validationResult.add("lane", "Lane data is missing");
            return validationResult;
        }

        if (object.laneName() == null || object.laneName().isBlank()) {
            validationResult.add("laneName", "Lane name is required");
        }

        if (object.fromLocation() == null || !ValidationUtils.isValidLocation(object.fromLocation())) {
            validationResult.add("fromLocation", "Origin is required");
        }

        if (object.toLocation() == null || !ValidationUtils.isValidLocation(object.toLocation())) {
            validationResult.add("toLocation", "Destination is required");
        }

        if (object.shipmentType() == null) {
            validationResult.add("shipmentType", "Shipment type is required");
        }

        if (object.transportType() == null) {
            validationResult.add("transportType", "Transport type is required");
        }

        if (object.transportType() == TransportTypeDto.REF) {
            if (!ValidationUtils.isValidTemperature(object.temperature())) {
                validationResult.add("temperature", "Invalid temperature format");
            }
        }

        if (!ValidationUtils.isValidWeight(object.weight())) {
            validationResult.add("weight", "Weight exceeds max value " + MAX_LOAD_CAPACITY + " kg");
        }

        if (object.price() == null) {
            validationResult.add("price", "Price is required");
        } else if (object.price().compareTo(BigDecimal.ZERO) <= 0) {
            validationResult.add("price", "Price must be greater than 0");
        }

        if (object.fuelSurcharge() == null ||
                object.fuelSurcharge().compareTo(new BigDecimal("-1.00")) < 0) {
            validationResult.add("fuelSurcharge", "Fuel surcharge cannot be less than -100%");
        }

        LocalDate from = object.validFrom();
        LocalDate to = object.validTo();

        if (from == null) {
            validationResult.add("validFrom", "Start date is required");
        }

        if (to == null) {
            validationResult.add("validTo", "End date is required");
        }

        if (from != null && to != null && to.isBefore(from)) {
            validationResult.add("validTo", "End date must be after start date");
        }

        if (object.type() == null) {
            validationResult.add("type", "Lane type is required");
        }
        return  validationResult;
    }
}