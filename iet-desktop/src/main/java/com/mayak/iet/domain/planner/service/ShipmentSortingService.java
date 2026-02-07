package com.mayak.iet.domain.planner.service;

import com.mayak.iet.shipment.dto.enums.ShipmentStatusDto;
import com.mayak.iet.shipment.dto.view.ShipmentListItemDto;
import com.mayak.iet.shipment.dto.view.ShipmentTimestampDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;

@Service
public class ShipmentSortingService {

    private static final Map<ShipmentStatusDto, Integer> STATUS_ORDER =
            Map.of(
                    ShipmentStatusDto.PLANNED, 0,
                    ShipmentStatusDto.LOADED, 1,
                    ShipmentStatusDto.DROPPED, 2,
                    ShipmentStatusDto.CANCELED, 3
            );

    public Comparator<ShipmentListItemDto> byStatus() {
        return Comparator.comparingInt(
                dto -> STATUS_ORDER.getOrDefault(dto.status(), 99)
        );
    }
}