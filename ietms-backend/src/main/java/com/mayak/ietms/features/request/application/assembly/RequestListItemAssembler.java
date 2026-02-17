package com.mayak.ietms.features.request.application.assembly;

import com.mayak.ietms.request.dto.enums.RequestStatusDto;
import com.mayak.ietms.request.dto.enums.RequestTypeDto;
import com.mayak.ietms.request.dto.enums.ShipmentTypeDto;
import com.mayak.ietms.request.dto.enums.TransportTypeDto;
import com.mayak.ietms.request.dto.view.RequestListItemDto;
import com.mayak.ietms.features.request.domain.model.ContractRequest;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.domain.model.SpotRequest;
import com.mayak.ietms.features.user.application.UserLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class RequestListItemAssembler {

    private final UserLookupService userLookupService;

        public RequestListItemDto toDto(Request r) {
            BigDecimal totalPrice = null;

            RequestTypeDto requestType;
            if (r instanceof ContractRequest contract) {
                totalPrice = contract.getTotalPrice();
                requestType = RequestTypeDto.CONTRACT;
            } else if (r instanceof SpotRequest spot) {
                totalPrice = spot.getClientPrice();
                requestType = RequestTypeDto.SPOT;
            } else {
                requestType = null;
            }

            return RequestListItemDto.builder()
                    .id(r.getId())
                    .requestType(requestType)
                    .customerReference(r.getCustomerReference())
                    .tId(r.getTid())
                    .customerName(r.getCustomer() != null ? r.getCustomer().getName() : null)
                    .startDate(r.getStartDate())
                    .endDate(r.getEndDate())
                    .shipmentType(mapShipmentType(r))
                    .transportType(mapTransportType(r))
                    .dangerous(r.isDangerous())
                    .temperature(r.getTemperature())
                    .weight(r.getWeight())
                    .loadingMeter(r.getLoadingMeter())
                    .comments(r.getComments())
                    .status(r.getStatus() != null ? RequestStatusDto.valueOf(r.getStatus().name()) : null)
                    .author(userLookupService.getName(r.getAuthorId()))
                    .issueDate(r.getIssueDate())
                    .totalPrice(totalPrice)
                    .dispatchedUser(userLookupService.getName(r.getDispatcherId()))
                    .build();
    }

    private ShipmentTypeDto mapShipmentType(Request r) {
        return r.getShipmentType() != null
                ? ShipmentTypeDto.valueOf(r.getShipmentType().name())
                : null;
    }

    private TransportTypeDto mapTransportType(Request r) {
        return r.getTransportType() != null
                ? TransportTypeDto.valueOf(r.getTransportType().name())
                : null;
    }
}