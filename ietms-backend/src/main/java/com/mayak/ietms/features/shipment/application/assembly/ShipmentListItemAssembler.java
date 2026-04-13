package com.mayak.ietms.features.shipment.application.assembly;

import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import com.mayak.ietms.features.shipment.domain.model.Shipment;
import com.mayak.ietms.features.shipment.infra.mapping.ShipmentMapper;
import com.mayak.ietms.features.location.application.LocationResolver;
import com.mayak.ietms.features.user.application.UserLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Builds ShipmentListItemDto reflecting shipment state as of given date.
 */

@Component
@RequiredArgsConstructor
public class ShipmentListItemAssembler {

    private final ShipmentMapper shipmentMapper;
    private final LocationResolver locationResolver;
    private final UserLookupService userLookupService;

    public ShipmentListItemDto assembleCurrent(Shipment shipment) {
        ShipmentListItemDto dto = shipmentMapper.toListItemDto(shipment);
        return enrichCommon(dto, shipment);
    }

    public ShipmentListItemDto assembleForPlanner(Shipment shipment, LocalDate date) {
        ShipmentListItemDto dto = shipmentMapper.toListItemDto(shipment);

        boolean isLastPlannedDay =
                shipment.getPlannedDropDate() != null && shipment.getPlannedDropDate().isEqual(date);

        var timestamps = isLastPlannedDay
                ? dto.timestamps()
                : dto.timestamps().stream()
                .filter(t -> !t.at().atZone(ZoneOffset.UTC).toLocalDate().isAfter(date))
                .toList();

        return enrichCommon(dto.withTimestamps(timestamps), shipment);
    }

    private ShipmentListItemDto enrichCommon(ShipmentListItemDto dto, Shipment shipment) {
        var from = locationResolver.resolve(shipment.getRequest().getFromLocationIds());
        var to   = locationResolver.resolve(shipment.getRequest().getToLocationIds());
        var author = userLookupService.getName(shipment.getRequest().getAuthorId());
        var dispatcher = shipment.getDispatcherId() != null
                        ? userLookupService.getName(shipment.getDispatcherId())
                        : null;

        return dto.withLocations(from, to).withAuthor(author.firstName(), author.lastName()).withDispatcher(dispatcher);
    }
}