package com.mayak.ietms.features.request.application.assembly;

import com.mayak.ietms.features.request.application.reason.RefuseReasonResolver;
import com.mayak.ietms.features.request.domain.enums.RequestStatus;
import com.mayak.ietms.request.dto.bid.BidViewDto;
import com.mayak.ietms.request.dto.enums.RequestStatusDto;
import com.mayak.ietms.request.dto.enums.RequestTypeDto;
import com.mayak.ietms.request.dto.enums.ShipmentTypeDto;
import com.mayak.ietms.request.dto.enums.TransportTypeDto;
import com.mayak.ietms.request.dto.refuse.RefuseReasonOptionDto;
import com.mayak.ietms.request.dto.view.RequestDetailsDto;
import com.mayak.ietms.features.bid.domain.model.Bid;
import com.mayak.ietms.features.request.domain.model.ContractRequest;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.domain.model.SpotRequest;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.features.bid.infra.mapping.BidMapper;
import com.mayak.ietms.features.company.infra.mapping.CompanyMapper;
import com.mayak.ietms.features.location.application.LocationResolver;
import com.mayak.ietms.features.request.application.access.RequestAccessService;
import com.mayak.ietms.features.user.application.UserLookupService;
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
                .dispatchedUser(userLookupService.toShortDto(request.getDispatcherId()))
                .competitors(userLookupService.toShortDtoSet(request.getCompetitorsId()))

                // -------- BIDS --------
                .bids(mapBids(request.getBids()))
                .activeBids(mapBids(activeBids))

                // -------- PRICES --------
                .profitMargin(resolveProfitMargin(request))
                .price(resolvePrice(request))
                .refuseReasons(resolveRefuseReasons(request))

                // -------- LANE --------
                .laneId(request instanceof ContractRequest c && c.getLane() != null ? c.getLane().getId() : null)
                .laneValidFrom(request instanceof ContractRequest c && c.getLane() != null ? c.getLane().getValidFrom() : null)
                .laneValidTo(request instanceof ContractRequest c && c.getLane() != null ? c.getLane().getValidTo() : null)

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

    private BigDecimal resolveProfitMargin(Request request) {
        if (request instanceof ContractRequest contract) {
            return contract.getProfitMargin();
        }
        return null;
    }

    private BigDecimal resolvePrice(Request request) {
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