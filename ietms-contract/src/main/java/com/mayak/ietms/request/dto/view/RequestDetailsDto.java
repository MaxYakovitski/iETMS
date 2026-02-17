package com.mayak.ietms.request.dto.view;

import com.mayak.ietms.request.dto.bid.BidViewDto;
import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.location.dto.LocationDto;
import com.mayak.ietms.request.dto.enums.RequestStatusDto;
import com.mayak.ietms.request.dto.enums.RequestTypeDto;
import com.mayak.ietms.request.dto.enums.ShipmentTypeDto;
import com.mayak.ietms.request.dto.enums.TransportTypeDto;
import com.mayak.ietms.request.dto.refuse.RefuseReasonOptionDto;
import com.mayak.ietms.user.dto.UserLookupDto;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Builder
public record RequestDetailsDto (
        Long version,
        Long id,
        RequestTypeDto requestType,
        Long laneId,

        boolean canBid,
        boolean canJoin,
        boolean isJoined,
        boolean isAuthor,

        String customerReference,

        List<LocationDto> fromLocations,
        List <LocationDto> toLocations,

        CompanyDto customer,

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
        boolean archived,

        Set<UserLookupDto> competitors,
        Set<BidViewDto> bids,
        Set<BidViewDto> activeBids,

        UserLookupDto dispatchedUser,
        UserLookupDto author,

        LocalDateTime issueDate,
        BigDecimal price,
        BigDecimal profitMargin,
        List<RefuseReasonOptionDto> refuseReasons,

        String tid) {
}
