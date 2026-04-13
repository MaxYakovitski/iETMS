package com.mayak.ietms.domain.planner.service;

import com.mayak.ietms.shipment.dto.enums.ShipmentStatusDto;
import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Map;

@Service
public class ShipmentSortingService {

    private static final Map<ShipmentStatusDto, Integer> STATUS_ORDER =
            Map.of(
                    ShipmentStatusDto.NEW, 0,
                    ShipmentStatusDto.PLANNED, 1,
                    ShipmentStatusDto.TO_LOAD, 2,
                    ShipmentStatusDto.LOADED, 3,
                    ShipmentStatusDto.TO_DROP, 4,
                    ShipmentStatusDto.DROPPED, 5,
                    ShipmentStatusDto.CANCELED, 6
            );

    public Comparator<ShipmentListItemDto> byDateThenStatus() {
        return Comparator
                .comparing(ShipmentListItemDto::startDate,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(dto -> STATUS_ORDER.getOrDefault(dto.status(), 99));
    }
}