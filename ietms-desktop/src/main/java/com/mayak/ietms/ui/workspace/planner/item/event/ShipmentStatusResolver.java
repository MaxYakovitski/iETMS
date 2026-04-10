package com.mayak.ietms.ui.workspace.planner.item.event;

import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import org.springframework.stereotype.Component;

@Component
public class ShipmentStatusResolver {

    public ShipmentStatusView resolve(ShipmentListItemDto dto) {
        return switch (dto.status()) {
            case NEW -> ShipmentStatusView.created();
            case PLANNED -> ShipmentStatusView.planned();
            case TO_LOAD -> ShipmentStatusView.toLoad();
            case LOADED -> ShipmentStatusView.loaded();
            case TO_DROP -> ShipmentStatusView.toDrop();
            case DROPPED -> ShipmentStatusView.dropped();
            case CANCELED -> ShipmentStatusView.canceled();
        };
    }
}