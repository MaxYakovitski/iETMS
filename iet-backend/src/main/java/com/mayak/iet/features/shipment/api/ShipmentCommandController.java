package com.mayak.iet.features.shipment.api;

import com.mayak.iet.shipment.dto.command.CancelShipmentRequest;
import com.mayak.iet.shipment.dto.view.ShipmentListItemDto;
import com.mayak.iet.shipment.dto.view.ShipmentUpdateDto;
import com.mayak.iet.features.shipment.domain.enums.ShipmentCancelReason;
import com.mayak.iet.infrastructure.security.current.CurrentUserId;
import com.mayak.iet.features.shipment.application.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentCommandController {

    private final ShipmentService shipmentService;

    @PatchMapping("/{id}")
    public ShipmentListItemDto update(
            @PathVariable("id") Long id,
            @RequestBody ShipmentUpdateDto dto,
            @CurrentUserId Long userId) {
        return shipmentService.update(id, dto, userId);
    }

    @PostMapping("/{id}/cancel")
    public void cancel(
            @PathVariable("id") Long id,
            @RequestBody CancelShipmentRequest request,
            @CurrentUserId Long userId
    ) {
        shipmentService.cancel(id, ShipmentCancelReason.valueOf(request.reason().name()), userId);
    }
}