package com.mayak.iet.ui.workspace.planner.item.event;

import com.mayak.iet.shipment.dto.view.ShipmentListItemDto;
import com.mayak.iet.shipment.dto.view.ShipmentTimestampDto;
import com.mayak.iet.infrastructure.common.TextUtils;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Optional;

@Component
public class ShipmentLastEventResolver {

    public Optional<ShipmentLastEvent> resolve(ShipmentListItemDto dto) {
        if (dto.timestamps() == null || dto.timestamps().isEmpty()) {
            return Optional.empty();
        }

        return dto.timestamps().stream()
                .filter(t -> t.at() != null)
                .max(Comparator.comparing(ShipmentTimestampDto::at))
                .map(t -> switch (t.status()) {
                    case PLANNED  -> new ShipmentLastEvent("PLANNED",  t.at(), TextUtils.SYSTEM_TEXT_DEFAULT_COLOR);
                    case LOADED   -> new ShipmentLastEvent("LOADED",   t.at(), TextUtils.SYSTEM_TEXT_DEFAULT_COLOR);
                    case DROPPED  -> new ShipmentLastEvent("DROPPED",  t.at(), TextUtils.SYSTEM_TEXT_GREEN_COLOR);
                    case CANCELED -> new ShipmentLastEvent("CANCELED", t.at(), TextUtils.SYSTEM_TEXT_RED_COLOR);
                });
    }
}