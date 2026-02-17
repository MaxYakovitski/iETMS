package com.mayak.iet.features.shipment.infra.mapping;

import com.mayak.iet.request.dto.event.ShipmentEventDto;
import com.mayak.iet.features.shipment.domain.model.Shipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShipmentEventMapper {

    @Mapping(target = "shipmentId", source = "id")
    @Mapping(target = "requestId", source = "id")
    ShipmentEventDto toEventDto(Shipment shipment);
}