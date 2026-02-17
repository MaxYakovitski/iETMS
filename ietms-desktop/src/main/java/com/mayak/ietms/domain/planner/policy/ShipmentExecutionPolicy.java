package com.mayak.ietms.domain.planner.policy;

import com.mayak.ietms.shipment.dto.enums.ShipmentStatusDto;
import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import org.springframework.stereotype.Service;

@Service
public class ShipmentExecutionPolicy {

    public boolean hasExecution(ShipmentListItemDto dto) {
        return hasStatus(dto, ShipmentStatusDto.LOADED)
                || hasStatus(dto, ShipmentStatusDto.DROPPED);
    }

    public boolean showTimeline(ShipmentListItemDto dto) {
        return dto.timestamps() != null && !dto.timestamps().isEmpty();
    }

    public boolean canCancel(ShipmentListItemDto dto) {
        if (!hasStatus(dto, ShipmentStatusDto.PLANNED)) return false;
        if (hasExecution(dto)) return false;

        if (dto.status() == ShipmentStatusDto.CANCELED) return false;

        return true;
    }

    private boolean hasStatus(ShipmentListItemDto dto, ShipmentStatusDto status) {
        return dto.timestamps()
                .stream()
                .anyMatch(t -> t.status() == status);
    }
}