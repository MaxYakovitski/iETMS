package com.mayak.ietms.lane.dto;

import com.mayak.ietms.location.dto.LocationDto;
import com.mayak.ietms.request.dto.enums.ShipmentTypeDto;
import com.mayak.ietms.request.dto.enums.TransportTypeDto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LaneCreateDto(String laneName,
                            LocationDto fromLocation,
                            LocationDto toLocation,
                            String temperature,
                            Double weight,
                            ShipmentTypeDto shipmentType,
                            TransportTypeDto transportType,
                            BigDecimal price,
                            BigDecimal fuelSurcharge,
                            LocalDate validFrom,
                            LocalDate validTo,
                            LaneTypeDto type) {
}