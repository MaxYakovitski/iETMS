package com.mayak.iet.domain.planner.policy;

import com.mayak.iet.domain.planner.model.ShipmentContext;
import com.mayak.iet.shipment.dto.enums.ShipmentStatusDto;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@Service
public class ShipmentPlannerPolicy {

    private static final Set<ShipmentStatusDto> FINAL_STATUSES =
            EnumSet.of(ShipmentStatusDto.DROPPED, ShipmentStatusDto.CANCELED);


    public boolean canEditTransportFields(ShipmentContext ctx) {
        if (FINAL_STATUSES.contains(ctx.status())) {
            return false;
        }

        return switch (ctx.status()) {
            case PLANNED -> true;
            case LOADED -> ctx.isDropDate();
            default -> false;
        };
    }

    public Optional<ShipmentStatusDto> allowedNextStatus(ShipmentContext ctx) {
        if (FINAL_STATUSES.contains(ctx.status())) {
            return Optional.empty();
        }

        return switch (ctx.status()) {
            case PLANNED ->
                    ctx.isLoadDate()
                            ? Optional.of(ShipmentStatusDto.LOADED)
                            : Optional.empty();

            case LOADED ->
                    ctx.isDropDate()
                            ? Optional.of(ShipmentStatusDto.DROPPED)
                            : Optional.empty();

            default -> Optional.empty();
        };
    }
}