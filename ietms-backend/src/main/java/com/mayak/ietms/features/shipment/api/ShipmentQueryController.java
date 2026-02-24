package com.mayak.ietms.features.shipment.api;

import com.mayak.ietms.shipment.dto.view.MyTransportEventDto;
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

    private final ShipmentService shipmentService;

    @GetMapping("/my-shipments")
    public List<ShipmentListItemDto> findMyShipments(@RequestParam("date") LocalDate date, @CurrentUserId Long userId) {
        return shipmentService.findMyShipmentsForDate(date, userId);
    }

    @GetMapping("/my-transport-events")
    public List<MyTransportEventDto> myTransportEvents(@RequestParam("date") LocalDate date, @CurrentUserId Long userId) {
        return shipmentService.findMyTransportEventsForDate(date, userId);
    }

    @GetMapping("/{id}")
    public ShipmentListItemDto getShipment(
            @PathVariable("id") Long id,
            @CurrentUserId Long userId
    ) {
        return shipmentService.getShipmentForUser(id, userId);
    }
}
