package com.mayak.ietms.features.shipment.application.assembly;

import com.mayak.ietms.location.dto.LocationDto;
import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import com.mayak.ietms.features.shipment.domain.model.Shipment;
import com.mayak.ietms.features.shipment.infra.mapping.ShipmentMapper;
import com.mayak.ietms.user.dto.UserNameDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Assembles {@link ShipmentListItemDto} from a {@link Shipment} entity.
 * Requires pre-loaded user names and location cache to avoid N+1 queries.
 */
@Component
@RequiredArgsConstructor
public class ShipmentListItemAssembler {

    private final ShipmentMapper shipmentMapper;

    public ShipmentListItemDto assembleCurrent(Shipment shipment,
                                               Map<Long, UserNameDto> userNames,
                                               Map<Long, LocationDto> locationCache) {

        ShipmentListItemDto dto = shipmentMapper.toListItemDto(shipment);
        return enrichCommon(dto, shipment, userNames, locationCache);
    }

    public ShipmentListItemDto assembleForPlanner(Shipment shipment,
                                                  LocalDate date,
                                                  Map<Long, UserNameDto> userNames,
                                                  Map<Long, LocationDto> locationCache) {

        ShipmentListItemDto dto = shipmentMapper.toListItemDto(shipment);
        boolean isLastPlannedDay = shipment.getPlannedDropDate() != null && shipment.getPlannedDropDate().isEqual(date);
        var timestamps = isLastPlannedDay
                ? dto.timestamps()
                : dto.timestamps().stream()
                .filter(t -> !t.at().atZone(ZoneOffset.UTC).toLocalDate().isAfter(date))
                .toList();
        return enrichCommon(dto.withTimestamps(timestamps), shipment, userNames, locationCache);
    }

    private ShipmentListItemDto enrichCommon(ShipmentListItemDto dto,
                                             Shipment shipment,
                                             Map<Long, UserNameDto> userNames,
                                             Map<Long, LocationDto> locationCache) {

        var fromLocationIds = shipment.getRequest().getFromLocationIds();
        var toLocationIds = shipment.getRequest().getToLocationIds();
        List<LocationDto> from = fromLocationIds == null ? List.of()
                : fromLocationIds.stream().map(locationCache::get).filter(Objects::nonNull).toList();
        List<LocationDto> to   = toLocationIds == null ? List.of()
                : toLocationIds.stream().map(locationCache::get).filter(Objects::nonNull).toList();
        var author = userNames.get(shipment.getRequest().getAuthorId());
        var dispatcher = shipment.getDispatcherId() != null ? userNames.get(shipment.getDispatcherId()) : null;
        return dto.withLocations(from, to).withAuthor(author.firstName(), author.lastName()).withDispatcher(dispatcher);
    }
}