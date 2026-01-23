package com.mayak.iet.features.shipment.infra.mapping;

import com.mayak.iet.shipment.dto.view.ShipmentTimestampDto;
import com.mayak.iet.features.shipment.domain.model.ShipmentTimeStamp;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentTimestampMapper {

    ShipmentTimestampDto toDto(ShipmentTimeStamp ts);
}