package com.mayak.ietms.domain.planner.service;

import com.mayak.ietms.shipment.dto.enums.ShipmentStatusDto;
import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Map;

@Service
public class TransportSortingService {

    private static final Map<ShipmentStatusDto, Integer> STATUS_ORDER =
            Map.of(
                    ShipmentStatusDto.NEW, 0,
                    ShipmentStatusDto.TO_DROP, 1,
                    ShipmentStatusDto.TO_LOAD, 2,
                    ShipmentStatusDto.PLANNED, 3,
                    ShipmentStatusDto.LOADED, 4,
                    ShipmentStatusDto.DROPPED, 5,
                    ShipmentStatusDto.CANCELED, 6
            );

    public Comparator<ShipmentListItemDto> byStatusThenDate() {
        return Comparator
                .comparingInt((ShipmentListItemDto dto) -> STATUS_ORDER.getOrDefault(dto.status(), 99))
                .thenComparing(dto -> {
                    boolean useDropDate =  dto.status() == ShipmentStatusDto.LOADED
                                        || dto.status() == ShipmentStatusDto.TO_DROP
                                        || dto.status() == ShipmentStatusDto.DROPPED;
                    return useDropDate ? dto.endDate() : dto.startDate();
                }, Comparator.nullsLast(Comparator.naturalOrder()));
    }
}