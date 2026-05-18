package com.mayak.ietms.features.shipment.api;

import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import com.mayak.ietms.infrastructure.security.current.CurrentUserId;
import com.mayak.ietms.features.shipment.application.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@Tag(name = "Shipments", description = "Shipment tracking and management")
@RequiredArgsConstructor
@Slf4j
public class ShipmentQueryController {

    private final ShipmentService service;

    @GetMapping("/my-shipments")
    @Operation(summary = "Find my shipments for date",
               description = "Returns shipments assigned to the current user for the given date.")
    public List<ShipmentListItemDto> findMyShipments(@RequestParam("date") LocalDate date, @CurrentUserId Long userId) {
        return service.findMyShipmentsForDate(date, userId);
    }

    @GetMapping("/my-transports")
    @Operation(summary = "Find my active transports",
               description = "Returns active transport assignments for the current user regardless of date.")
    public List<ShipmentListItemDto> myTransports(@CurrentUserId Long userId) {
        return service.findMyActiveTransports(userId);
    }

    @GetMapping("/{id}")
    public ShipmentListItemDto getDetails(@PathVariable("id") Long id, @CurrentUserId Long userId) {
        return service.getDetails(id, userId);
    }
}