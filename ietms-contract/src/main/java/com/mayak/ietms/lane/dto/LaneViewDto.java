package com.mayak.ietms.lane.dto;

import com.mayak.ietms.location.dto.LocationDto;
import com.mayak.ietms.request.dto.enums.ShipmentTypeDto;
import com.mayak.ietms.request.dto.enums.TransportTypeDto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LaneViewDto(Long id,
                          String laneName,
                          LocationDto fromLocation,
                          LocationDto toLocation,
                          ShipmentTypeDto shipmentType,
                          TransportTypeDto transportType,
                          String temperature,
                          Double weight,
                          LaneTypeDto type,
                          BigDecimal price,
                          BigDecimal fuelSurcharge,
                          BigDecimal totalPrice,
                          LocalDate validFrom,
                          LocalDate validTo) {
}