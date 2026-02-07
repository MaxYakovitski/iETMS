package com.mayak.iet.domain.planner.service;

import com.mayak.iet.integration.api.ShipmentClient;
import com.mayak.iet.shipment.dto.enums.TransportEventType;
import com.mayak.iet.shipment.dto.view.MyTransportEventDto;
import com.mayak.iet.shipment.dto.view.ShipmentListItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlannerDataService {

    private final ShipmentClient shipmentClient;
    private final ShipmentSortingService shipmentSortingService;

    public List<ShipmentListItemDto> loadMyShipments(LocalDate date) {
        LocalDate d = date != null ? date : LocalDate.now();

        return shipmentClient.findMyShipments(d).stream()
                .sorted(shipmentSortingService.byStatusThenTime(d))
                .toList();
    }

    public TransportBuckets loadMyTransports(LocalDate date) {
        LocalDate d = date != null ? date : LocalDate.now();

        List<MyTransportEventDto> events = shipmentClient.findMyTransportEvents(d);

        List<ShipmentListItemDto> toLoad = events.stream()
                .filter(e -> e.eventType() == TransportEventType.LOAD)
                .map(MyTransportEventDto::shipment)
                .sorted(shipmentSortingService.byStatusThenTime(d))
                .toList();

        List<ShipmentListItemDto> toDrop = events.stream()
                .filter(e -> e.eventType() == TransportEventType.DROP)
                .map(MyTransportEventDto::shipment)
                .sorted(shipmentSortingService.byStatusThenTime(d))
                .toList();

        return new TransportBuckets(toLoad, toDrop);
    }

    public record TransportBuckets(List<ShipmentListItemDto> toLoad, List<ShipmentListItemDto> toDrop) {}
}
