package com.mayak.ietms.features.shipment.api;

import com.mayak.ietms.shipment.dto.command.CancelShipmentRequest;
import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import com.mayak.ietms.shipment.dto.view.ShipmentUpdateDto;
import com.mayak.ietms.features.shipment.domain.enums.ShipmentCancelReason;
import com.mayak.ietms.infrastructure.security.current.CurrentUserId;
import com.mayak.ietms.features.shipment.application.ShipmentService;
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