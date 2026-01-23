package com.mayak.iet.features.shipment.infra.mapping;

import com.mayak.iet.shipment.dto.view.ShipmentListItemDto;
import com.mayak.iet.features.shipment.domain.model.Shipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = ShipmentTimestampMapper.class)
public interface ShipmentMapper {

    @Mapping(target = "requestType", expression = "java(shipment.getRequest().getRequestTypeDto())")
    @Mapping(target = "customerReference", source = "request.customerReference")
    @Mapping(target = "tId", source = "request.tid")
    @Mapping(target = "customerName", source = "request.customer.name")
    @Mapping(target = "startDate", source = "request.startDate")
    @Mapping(target = "endDate", source = "request.endDate")
    @Mapping(target = "shipmentType", source = "request.shipmentType")
    @Mapping(target = "transportType", source = "request.transportType")
    @Mapping(target = "dangerous", source = "request.dangerous")
    @Mapping(target = "temperature", source = "request.temperature")
    @Mapping(target = "weight", source = "request.weight")
    @Mapping(target = "loadingMeter", source = "request.loadingMeter")
    @Mapping(target = "requestComments", source = "request.comments")
    @Mapping(target = "clientPrice", source = "request.clientPrice")
    @Mapping(target = "carrierPrice", source = "request.bidPrice")

    // shipment fields
    @Mapping(target = "status", source = "status")
    @Mapping(target = "timestamps", source = "timestamps")
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "dispatcher", ignore = true)
    @Mapping(target = "carrierName", source = "carrier.name")
    @Mapping(target = "shipmentComments", source = "comments")
    ShipmentListItemDto toListItemDto(Shipment shipment);

}