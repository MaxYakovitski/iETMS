package com.mayak.iet.lane.dto;

import com.mayak.iet.location.dto.LocationDto;
import com.mayak.iet.request.dto.enums.ShipmentTypeDto;
import com.mayak.iet.request.dto.enums.TransportTypeDto;

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