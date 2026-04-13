package com.mayak.ietms.features.shipment.api;

import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import com.mayak.ietms.infrastructure.security.current.CurrentUserId;
import com.mayak.ietms.features.shipment.application.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
@Slf4j
public class ShipmentQueryController {

    private final ShipmentService service;

    @GetMapping("/my-shipments")
    public List<ShipmentListItemDto> findMyShipments(@RequestParam("date") LocalDate date, @CurrentUserId Long userId) {
        return service.findMyShipmentsForDate(date, userId);
    }

    @GetMapping("/my-transports")
    public List<ShipmentListItemDto> myTransports(@CurrentUserId Long userId) {
        return service.findMyActiveTransports(userId);
    }

    @GetMapping("/{id}")
    public ShipmentListItemDto getDetails(@PathVariable("id") Long id, @CurrentUserId Long userId) {
        return service.getDetails(id, userId);
    }
}