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

    public Comparator<ShipmentListItemDto> byStatusThenTime(LocalDate date) {
        return Comparator
                .comparing(
                        (ShipmentListItemDto dto) -> resolveStatusRank(dto, date)
                )
                .thenComparing(
                        dto -> resolveLastTimestamp(dto, date),
                        Comparator.reverseOrder()
                );
    }

    private int resolveStatusRank(ShipmentListItemDto dto, LocalDate date) {
        ShipmentTimestampDto last = resolveLastTimestampDto(dto, date);
        return last == null
                ? 99
                : STATUS_ORDER.getOrDefault(last.status(), 99);
    }

    private LocalDateTime resolveLastTimestamp(ShipmentListItemDto dto, LocalDate date) {
        ShipmentTimestampDto ts = resolveLastTimestampDto(dto, date);
        return ts != null ? ts.at() : LocalDateTime.MIN;
    }

    private ShipmentTimestampDto resolveLastTimestampDto(
            ShipmentListItemDto dto,
            LocalDate date
    ) {
        return dto.timestamps().stream()
                .filter(t -> !t.at().toLocalDate().isAfter(date))
                .max(Comparator.comparing(ShipmentTimestampDto::at))
                .orElse(null);
    }
}