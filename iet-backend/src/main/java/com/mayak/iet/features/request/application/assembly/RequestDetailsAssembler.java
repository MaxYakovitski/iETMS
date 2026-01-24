package com.mayak.iet.features.request.application.assembly;

import com.mayak.iet.features.request.application.reason.RefuseReasonResolver;
import com.mayak.iet.features.request.domain.enums.RequestStatus;
import com.mayak.iet.request.dto.bid.BidViewDto;
import com.mayak.iet.request.dto.enums.RequestStatusDto;
import com.mayak.iet.request.dto.enums.RequestTypeDto;
import com.mayak.iet.request.dto.enums.ShipmentTypeDto;
import com.mayak.iet.request.dto.enums.TransportTypeDto;
import com.mayak.iet.request.dto.refuse.RefuseReasonOptionDto;
import com.mayak.iet.request.dto.view.RequestDetailsDto;
import com.mayak.iet.features.bid.domain.model.Bid;
import com.mayak.iet.features.request.domain.model.ContractRequest;
import com.mayak.iet.features.request.domain.model.Request;
import com.mayak.iet.features.request.domain.model.SpotRequest;
import com.mayak.iet.features.user.domain.model.User;
import com.mayak.iet.features.bid.infra.mapping.BidMapper;
import com.mayak.iet.features.company.infra.mapping.CompanyMapper;
import com.mayak.iet.features.location.application.LocationResolver;
import com.mayak.iet.features.request.application.access.RequestAccessService;
import com.mayak.iet.features.user.application.UserLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RequestDetailsAssembler {

    private final RequestAccessService requestAccessService;
    private final LocationResolver locationResolver;
    private final CompanyMapper companyMapper;
    private final UserLookupService userLookupService;
    private final BidMapper bidMapper;
    private final RefuseReasonResolver refuseReasonResolver;

    public RequestDetailsDto toDto(Request request, User user, Set<Bid> activeBids) {

        RequestTypeDto requestType = resolveRequestType(request);

        return RequestDetailsDto.builder()
                // -------- META --------
                .version(request.getVersion())
                .id(request.getId())
                .requestType(requestType)

                // -------- ACCESS FLAGS --------
                .canBid(requestAccessService.canBid(user, request))
                .canJoin(requestAccessService.canJoin(user, request))
                .isJoined(requestAccessService.isJoined(user, request))
                .isAuthor(user != null && request.isAuthoredBy(user.getId()))

                // -------- BASE --------
                .customerReference(request.getCustomerReference())

                // -------- LOCATIONS --------
                .fromLocations(locationResolver.resolve(request.getFromLocationIds()))
                .toLocations(locationResolver.resolve(request.getToLocationIds()))

                // -------- COMPANY --------
                .customer(request.getCustomer() != null ? companyMapper.toDto(request.getCustomer()) : null)

                // -------- DATES --------
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .issueDate(request.getIssueDate())

                // -------- TRANSPORT --------
                .shipmentType(mapShipmentType(request))
                .transportType(mapTransportType(request))
                .dangerous(request.isDangerous())
                .temperature(request.getTemperature())
                .weight(request.getWeight())
                .loadingMeter(request.getLoadingMeter())
                .comments(request.getComments())

                // -------- STATUS --------
                .status(request.getStatus() != null ? RequestStatusDto.valueOf(request.getStatus().name()) : null)
                .archived(request.isArchived())
                .tid(request.getTid())

                // -------- USERS --------
                .author(userLookupService.toShortDto(request.getAuthorId()))
                .assignedUser(userLookupService.toShortDto(request.getDispatcherId()))
                .competitors(userLookupService.toShortDtoSet(request.getCompetitorsId()))

                // -------- BIDS --------
                .bids(mapBids(request.getBids()))
                .activeBids(mapBids(activeBids))

                // -------- PRICES --------
                .bidPrice(resolveBidPrice(request))
                .profitMargin(resolveProfitMargin(request))
                .displayPrice(resolveDisplayPrice(request))
                .refuseReasons(resolveRefuseReasons(request))

                // -------- LANE --------
                .laneId(request instanceof ContractRequest c && c.getLane() != null ? c.getLane().getId() : null)

                .build();
    }

    // ==================== HELPERS ====================

    private RequestTypeDto resolveRequestType(Request request) {
        if (request instanceof SpotRequest) return RequestTypeDto.SPOT;
        if (request instanceof ContractRequest) return RequestTypeDto.CONTRACT;
        return null;
    }

    private ShipmentTypeDto mapShipmentType(Request request) {
        return request.getShipmentType() != null
                ? ShipmentTypeDto.valueOf(request.getShipmentType().name())
                : null;
    }

    private TransportTypeDto mapTransportType(Request request) {
        return request.getTransportType() != null
                ? TransportTypeDto.valueOf(request.getTransportType().name())
                : null;
    }

    private Set<BidViewDto> mapBids(Set<Bid> bids) {
        if (bids == null || bids.isEmpty()) {
            return Set.of();
        }
        return bidMapper.toViewDtoSet(bids);
    }

    private BigDecimal resolveBidPrice(Request request) {
        if (request instanceof ContractRequest contract) {
            return contract.getTotalPrice();
        }
        return null;
    }

    private BigDecimal resolveProfitMargin(Request request) {
        if (request instanceof ContractRequest contract) {
            return contract.getProfitMargin();
        }
        return null;
    }

    private BigDecimal resolveDisplayPrice(Request request) {
        return switch (request) {
            case ContractRequest c -> c.getTotalPrice();
            case SpotRequest s -> s.getClientPrice();
            default -> null;
        };
    }

    private List<RefuseReasonOptionDto> resolveRefuseReasons(Request request) {
        if (request.getStatus() == RequestStatus.OFFERED) {
            return refuseReasonResolver.resolve(request);
        }
        return List.of();
    }
}