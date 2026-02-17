package com.mayak.ietms.ui.workspace.planner.item.event;

import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import org.springframework.stereotype.Component;

@Component
public class ShipmentStatusResolver {

    public ShipmentStatusView resolve(ShipmentListItemDto dto) {
        return switch (dto.status()) {
            case PLANNED -> ShipmentStatusView.planned();
            case LOADED -> ShipmentStatusView.loaded();
            case DROPPED -> ShipmentStatusView.dropped();
            case CANCELED -> ShipmentStatusView.canceled();
        };
    }
}