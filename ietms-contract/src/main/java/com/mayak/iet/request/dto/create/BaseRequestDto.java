package com.mayak.iet.request.dto.create;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mayak.iet.request.dto.enums.RequestTypeDto;
import com.mayak.iet.request.dto.enums.ShipmentTypeDto;
import com.mayak.iet.request.dto.enums.TransportTypeDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SpotRequestDto.class, name = "SPOT"),
        @JsonSubTypes.Type(value = ContractRequestDto.class, name = "CONTRACT")
})
public class BaseRequestDto {
    RequestTypeDto type;

    List <String> fromLocations;
    List <String> toLocations;

    String customerName;
    String customerReference;

    LocalDateTime startDate;
    LocalDateTime endDate;

    ShipmentTypeDto shipmentType;
    TransportTypeDto transportType;

    boolean dangerous;
    String temperature;
    Double weight;
    Double loadingMeter;
    String comments;
}