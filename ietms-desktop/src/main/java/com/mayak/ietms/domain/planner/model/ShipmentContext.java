package com.mayak.ietms.domain.planner.model;

import com.mayak.ietms.shipment.dto.enums.ShipmentStatusDto;
import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;

import java.time.LocalDate;

public record ShipmentContext (ShipmentStatusDto status, LocalDate selectedDate, LocalDate loadDate, LocalDate dropDate) {

    public static ShipmentContext from(ShipmentListItemDto dto, LocalDate selectedDate) {
        return new ShipmentContext(
                dto.status(),
                selectedDate,
                dto.startDate().toLocalDate(),
                dto.endDate().toLocalDate()
        );
    }

    public boolean isLoadDate() {
        return selectedDate != null && selectedDate.equals(loadDate);
    }

    public boolean isDropDate() {
        return selectedDate != null && selectedDate.equals(dropDate);
    }

    public boolean isFinal() {
        return status == ShipmentStatusDto.DROPPED
                || status == ShipmentStatusDto.CANCELED;
    }
}