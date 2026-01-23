package com.mayak.iet.features.shipment.api;

import com.mayak.iet.shipment.dto.view.MyTransportEventDto;
import com.mayak.iet.shipment.dto.view.ShipmentListItemDto;
import com.mayak.iet.infrastructure.security.current.CurrentUserId;
import com.mayak.iet.features.shipment.application.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
