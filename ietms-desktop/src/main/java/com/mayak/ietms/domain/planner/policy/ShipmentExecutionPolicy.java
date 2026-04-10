package com.mayak.ietms.domain.planner.policy;

import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import org.springframework.stereotype.Service;

@Service
public class ShipmentExecutionPolicy {

    public boolean showTimeline(ShipmentListItemDto dto) {
        return dto.timestamps() != null && !dto.timestamps().isEmpty();
    }

    public boolean canCancel(ShipmentListItemDto dto) {
        return dto.status().isPreLoad();
    }

}