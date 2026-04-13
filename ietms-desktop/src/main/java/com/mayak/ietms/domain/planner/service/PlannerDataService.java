package com.mayak.ietms.domain.planner.service;

import com.mayak.ietms.integration.api.ShipmentClient;
import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlannerDataService {

    private final ShipmentClient shipmentClient;
    private final ShipmentSortingService shipmentSortingService;
    private final TransportSortingService transportSortingService;

    public List<ShipmentListItemDto> loadMyShipments(LocalDate date) {
        LocalDate d = date != null ? date : LocalDate.now();

        return shipmentClient.findMyShipments(d).stream()
                .sorted(shipmentSortingService.byDateThenStatus())
                .toList();
    }

    public List<ShipmentListItemDto> loadMyTransports() {
        return shipmentClient.findMyTransports().stream()
                .sorted(transportSortingService.byStatusThenDate())
                .toList();
    }

    public ShipmentListItemDto loadShipmentById(Long shipmentId) {
        return shipmentClient.getDetails(shipmentId);
    }
}