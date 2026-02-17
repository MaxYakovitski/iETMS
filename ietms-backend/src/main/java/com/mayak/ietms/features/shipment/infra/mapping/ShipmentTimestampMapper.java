package com.mayak.ietms.features.shipment.infra.mapping;

import com.mayak.ietms.shipment.dto.view.ShipmentTimestampDto;
import com.mayak.ietms.features.shipment.domain.model.ShipmentTimeStamp;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentTimestampMapper {

    ShipmentTimestampDto toDto(ShipmentTimeStamp ts);
}