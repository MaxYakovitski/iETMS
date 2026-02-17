package com.mayak.iet.ui.workspace.planner.item.event;

import com.mayak.iet.shipment.dto.view.ShipmentListItemDto;
import com.mayak.iet.shipment.dto.view.ShipmentTimestampDto;
import com.mayak.iet.infrastructure.common.TextUtils;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Optional;

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