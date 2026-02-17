package com.mayak.ietms.request.validator;

import com.mayak.ietms.request.dto.create.BaseRequestDto;
import com.mayak.ietms.request.dto.create.ContractRequestDto;
import com.mayak.ietms.request.dto.enums.ShipmentTypeDto;
import com.mayak.ietms.request.dto.enums.TransportTypeDto;
import static com.mayak.ietms.common.domain.CargoConstraints.*;
import com.mayak.ietms.common.validation.ValidationUtils;
import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.common.validation.Validator;

public class RequestContractValidator implements Validator<BaseRequestDto> {

    @Override
    public ValidationResult isValid(BaseRequestDto object) {
        var validationResult = new ValidationResult();

        if (object == null || object.getType() == null) {
            validationResult.add("requestType", "Please select request type");
            return validationResult;
        }

        if (object instanceof ContractRequestDto contract) {
            if (contract.getCustomerName() == null) {
                validationResult.add("customerName", "Customer must be selected for contract request");
            }
            if (contract.getLaneId() == null) {
                validationResult.add("lane", "Lane must be selected for contract request");
            }
        }

        if (object.getFromLocations() == null || object.getFromLocations().isEmpty() ||
                object.getFromLocations().stream().anyMatch(loc -> !ValidationUtils.isValidLocation(loc))) {
            validationResult.add("fromLocations", "Invalid location(s) - from");
        }

        if (object.getToLocations() == null || object.getToLocations().isEmpty() ||
                object.getToLocations().stream().anyMatch(loc -> !ValidationUtils.isValidLocation(loc))) {
            validationResult.add("toLocations", "Invalid location(s) - to");
        }

        if (object.getStartDate() == null) {
            validationResult.add("startDate", "Start date is required");
        }
        if (object.getEndDate() == null) {
            validationResult.add("endDate", "End date is required");
        }

        if (object.getShipmentType() == null) {
            validationResult.add("shipmentType", "Please select shipment type");
        }

        if (object.getShipmentType() == ShipmentTypeDto.LTL) {
            if (!ValidationUtils.isValidLoadingMeter(object.getLoadingMeter())) {
                validationResult.add("loadingMeter", "Loading meter exceeds max " +  MAX_LOADING_METER_LTL + " ldm");
            }
        } else {
            if (object.getLoadingMeter() != null) {
                validationResult.add("loadingMeter", "Loading meter allowed only for LTL");
            }
        }

        if (object.getTransportType() == null) {
            validationResult.add("transportType", "Transport type is required");
        }

        if (!ValidationUtils.isValidWeight(object.getWeight())) {
            validationResult.add("weight", "Weight exceeds max value " + MAX_LOAD_CAPACITY + " kg");
        }

        if (object.getTransportType() == TransportTypeDto.REF) {
            if (!ValidationUtils.isValidTemperature(object.getTemperature())) {
                validationResult.add("temperature", "Invalid temperature format");
            }
        } else {
            if (object.getTemperature() != null) {
                validationResult.add("temperature", "Temperature allowed only for REF transport");
            }
        }
        return validationResult;
    }
}