package com.mayak.iet.request.dto.view;

import com.mayak.iet.request.dto.enums.RequestStatusDto;
import com.mayak.iet.request.dto.enums.RequestTypeDto;
import com.mayak.iet.request.dto.enums.ShipmentTypeDto;
import com.mayak.iet.request.dto.enums.TransportTypeDto;
import com.mayak.iet.user.dto.UserNameDto;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record RequestListItemDto(
        Long id,
        RequestTypeDto requestType,
        String customerReference,
        List<String> fromLocations,
        List<String> toLocations,
        String tId,
        String customerName,
        LocalDateTime startDate,
        LocalDateTime endDate,
        ShipmentTypeDto shipmentType,
        TransportTypeDto transportType,
        boolean dangerous,
        String temperature,
        Double weight,
        Double loadingMeter,
        String comments,
        RequestStatusDto status,
        UserNameDto author,
        LocalDateTime issueDate,

        BigDecimal totalPrice,
        UserNameDto dispatchedUser) {
}