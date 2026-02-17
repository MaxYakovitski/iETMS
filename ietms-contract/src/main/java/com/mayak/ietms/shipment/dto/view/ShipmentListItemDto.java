package com.mayak.ietms.shipment.dto.view;

import com.mayak.ietms.location.dto.LocationDto;
import com.mayak.ietms.request.dto.enums.RequestTypeDto;
import com.mayak.ietms.request.dto.enums.ShipmentTypeDto;
import com.mayak.ietms.request.dto.enums.TransportTypeDto;
import com.mayak.ietms.shipment.dto.enums.ShipmentStatusDto;
import com.mayak.ietms.user.dto.UserNameDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ShipmentListItemDto(
        Long id,

        RequestTypeDto requestType,
        String customerReference,
        List<LocationDto> fromLocations,
        List<LocationDto> toLocations,
        String tId,
        String customerName,

        LocalDateTime startDate,
        LocalDateTime endDate,

        ShipmentTypeDto shipmentType,
        TransportTypeDto transportType,

        boolean dangerous,
        String temperature,
        Double weight,
        Double loadingMeter,

        String requestComments,
        UserNameDto author,
        UserNameDto dispatcher,

        ShipmentStatusDto status,
        List<ShipmentTimestampDto> timestamps,

        BigDecimal clientPrice,
        BigDecimal carrierPrice,
        String carrierName,
        String licensePlate,
        String transportOrder,

        String shipmentComments) {

    public ShipmentListItemDto withLocations(List<LocationDto> fromLocations, List<LocationDto> toLocations) {
        return new ShipmentListItemDto(
                id,
                requestType,
                customerReference,
                fromLocations,
                toLocations,
                tId,
                customerName,
                startDate,
                endDate,
                shipmentType,
                transportType,
                dangerous,
                temperature,
                weight,
                loadingMeter,
                requestComments,
                author,
                dispatcher,
                status,
                timestamps,
                clientPrice,
                carrierPrice,
                carrierName,
                licensePlate,
                transportOrder,
                shipmentComments
        );
    }

    public ShipmentListItemDto withAuthor(String firstName, String lastName) {
        return new ShipmentListItemDto(
                id,
                requestType,
                customerReference,
                fromLocations,
                toLocations,
                tId,
                customerName,
                startDate,
                endDate,
                shipmentType,
                transportType,
                dangerous,
                temperature,
                weight,
                loadingMeter,
                requestComments,
                new UserNameDto(firstName, lastName),
                dispatcher,
                status,
                timestamps,
                clientPrice,
                carrierPrice,
                carrierName,
                licensePlate,
                transportOrder,
                shipmentComments
        );
    }

    public ShipmentListItemDto withDispatcher(UserNameDto dispatcher) {
        return new ShipmentListItemDto(
                id,
                requestType,
                customerReference,
                fromLocations,
                toLocations,
                tId,
                customerName,
                startDate,
                endDate,
                shipmentType,
                transportType,
                dangerous,
                temperature,
                weight,
                loadingMeter,
                requestComments,
                author,
                dispatcher,
                status,
                timestamps,
                clientPrice,
                carrierPrice,
                carrierName,
                licensePlate,
                transportOrder,
                shipmentComments
        );
    }

    public ShipmentListItemDto withTimestamps(List<ShipmentTimestampDto> timestamps) {
        return new ShipmentListItemDto(
                id,
                requestType,
                customerReference,
                fromLocations,
                toLocations,
                tId,
                customerName,
                startDate,
                endDate,
                shipmentType,
                transportType,
                dangerous,
                temperature,
                weight,
                loadingMeter,
                requestComments,
                author,
                dispatcher,
                status,
                timestamps,
                clientPrice,
                carrierPrice,
                carrierName,
                licensePlate,
                transportOrder,
                shipmentComments
        );
    }
}