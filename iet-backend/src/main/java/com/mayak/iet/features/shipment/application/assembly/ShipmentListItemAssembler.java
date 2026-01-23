package com.mayak.iet.features.shipment.application.assembly;

import com.mayak.iet.shipment.dto.view.ShipmentListItemDto;
import com.mayak.iet.features.shipment.domain.model.Shipment;
import com.mayak.iet.features.shipment.infra.mapping.ShipmentMapper;
import com.mayak.iet.features.location.application.LocationResolver;
import com.mayak.iet.features.user.application.UserLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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

    public ShipmentListItemDto assembleAsOfDate(Shipment shipment, LocalDate date) {
        ShipmentListItemDto dto = shipmentMapper.toListItemDto(shipment);

        var filteredTimestamps = dto.timestamps().stream()
                .filter(t -> !t.at().toLocalDate().isAfter(date))
                .toList();

        return enrichCommon(dto.withTimestamps(filteredTimestamps), shipment);
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