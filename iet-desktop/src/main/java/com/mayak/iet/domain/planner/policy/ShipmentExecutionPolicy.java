package com.mayak.iet.domain.planner.policy;

import com.mayak.iet.shipment.dto.enums.ShipmentStatusDto;
import com.mayak.iet.shipment.dto.view.ShipmentListItemDto;
import org.springframework.stereotype.Service;

@Service
public class ShipmentExecutionPolicy {

    public boolean hasExecution(ShipmentListItemDto dto) {
        return hasStatus(dto, ShipmentStatusDto.LOADED)
                || hasStatus(dto, ShipmentStatusDto.DROPPED);
    }

    public boolean showTimeline(ShipmentListItemDto dto) {
        return hasExecution(dto)
                || hasStatus(dto, ShipmentStatusDto.CANCELED);
    }

    public boolean canCancel(ShipmentListItemDto dto) {
        return hasStatus(dto, ShipmentStatusDto.PLANNED)
                && !hasExecution(dto)
                && !hasStatus(dto, ShipmentStatusDto.CANCELED);
    }

    private boolean hasStatus(ShipmentListItemDto dto, ShipmentStatusDto status) {
        return dto.timestamps()
                .stream()
                .anyMatch(t -> t.status() == status);
    }

    public boolean existsAsOfDate(ShipmentListItemDto dto) {
        return hasStatus(dto, ShipmentStatusDto.PLANNED);
    }
}