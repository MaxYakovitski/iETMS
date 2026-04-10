package com.mayak.ietms.domain.planner.policy;

import com.mayak.ietms.domain.planner.model.ShipmentContext;
import com.mayak.ietms.shipment.dto.enums.ShipmentStatusDto;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@Service
public class ShipmentPlannerPolicy {

    private static final Set<ShipmentStatusDto> FINAL_STATUSES =
            EnumSet.of(ShipmentStatusDto.DROPPED, ShipmentStatusDto.CANCELED);


    public boolean canEditCarrierFields(ShipmentContext ctx) {
        return ctx.status().isPreLoad();
    }

    public Optional<ShipmentStatusDto> allowedNextStatus(ShipmentContext ctx) {
        if (FINAL_STATUSES.contains(ctx.status())) {
            return Optional.empty();
        }

        return switch (ctx.status()) {
            case PLANNED, TO_LOAD -> Optional.of(ShipmentStatusDto.LOADED);
            case LOADED, TO_DROP -> Optional.of(ShipmentStatusDto.DROPPED);
            default -> Optional.empty();
        };
    }
}