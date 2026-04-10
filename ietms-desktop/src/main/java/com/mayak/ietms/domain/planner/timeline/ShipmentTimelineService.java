package com.mayak.ietms.domain.planner.timeline;

import com.mayak.ietms.shipment.dto.enums.ShipmentStatusDto;
import com.mayak.ietms.shipment.dto.view.ShipmentTimestampDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ShipmentTimelineService {

    private static final Map<ShipmentStatusDto, Integer> STATUS_ORDER =
            Map.of(
                    ShipmentStatusDto.NEW,      0,
                    ShipmentStatusDto.PLANNED,  1,
                    ShipmentStatusDto.TO_LOAD,  2,
                    ShipmentStatusDto.LOADED,   3,
                    ShipmentStatusDto.TO_DROP,  4,
                    ShipmentStatusDto.DROPPED,  5,
                    ShipmentStatusDto.CANCELED, 6
            );

    private static final Comparator<ShipmentTimestampDto> COMPARATOR =
            Comparator.comparing((ShipmentTimestampDto t) -> STATUS_ORDER.getOrDefault(t.status(), 99))
                    .thenComparing(ShipmentTimestampDto::at);

    public List<TimelineEntry> build(List<ShipmentTimestampDto> timestamps) {
        return timestamps.stream()
                .collect(Collectors.toMap(
                        ShipmentTimestampDto::status,
                        t -> t,
                        (existing, next) -> next.at().isAfter(existing.at()) ? next : existing))
                .values()
                .stream()
                .sorted(COMPARATOR)
                .map(this::toEntry)
                .toList();
    }

    private TimelineEntry toEntry(ShipmentTimestampDto t) {
        LocalDateTime localTime = t.at().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return new TimelineEntry(
                t.status().name(),
                localTime,
                t.status() == ShipmentStatusDto.CANCELED
                        ? TimelineColor.ERROR
                        : TimelineColor.SUCCESS
        );
    }
}