package com.mayak.ietms.domain.planner.model;

import com.mayak.ietms.shipment.dto.enums.ShipmentStatusDto;
import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;

import java.time.LocalDate;

public record ShipmentContext (ShipmentStatusDto status, LocalDate selectedDate) {

    public static ShipmentContext from(ShipmentListItemDto dto, LocalDate selectedDate) {
        return new ShipmentContext(dto.status(), selectedDate);
    }

    public boolean isFinal() {
        return status == ShipmentStatusDto.DROPPED || status == ShipmentStatusDto.CANCELED;
    }
}