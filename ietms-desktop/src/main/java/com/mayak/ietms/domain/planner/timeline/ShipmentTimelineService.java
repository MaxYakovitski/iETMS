package com.mayak.ietms.domain.planner.timeline;

import com.mayak.ietms.shipment.dto.enums.ShipmentStatusDto;
import com.mayak.ietms.shipment.dto.view.ShipmentTimestampDto;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class ShipmentTimelineService {

    private static final Map<ShipmentStatusDto, Integer> STATUS_ORDER =
            Map.of(
                    ShipmentStatusDto.PLANNED, 0,
                    ShipmentStatusDto.LOADED, 1,
                    ShipmentStatusDto.DROPPED, 2,
                    ShipmentStatusDto.CANCELED, 3
            );

    private static final Comparator<ShipmentTimestampDto> COMPARATOR =
            Comparator.comparing((ShipmentTimestampDto t) -> STATUS_ORDER.getOrDefault(t.status(), 99))
                    .thenComparing(ShipmentTimestampDto::at);

    public List<TimelineEntry> build(List<ShipmentTimestampDto> timestamps) {
        return timestamps.stream()
                .sorted(COMPARATOR)
                .map(this::toEntry)
                .toList();
    }

    private TimelineEntry toEntry(ShipmentTimestampDto t) {
        return new TimelineEntry(
                t.status().name(),
                t.at(),
                t.status() == ShipmentStatusDto.CANCELED
                        ? TimelineColor.ERROR
                        : TimelineColor.SUCCESS
        );
    }
}