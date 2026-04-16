package com.mayak.ietms.features.request.application.lifecycle;

import com.mayak.ietms.features.request.domain.lifecycle.RequestLifecycle;
import com.mayak.ietms.features.request.domain.model.RefuseReason;
import com.mayak.ietms.features.request.infra.mapping.RefuseReasonMapper;
import com.mayak.ietms.features.shipment.application.notify.ShipmentNotificationService;
import com.mayak.ietms.features.user.application.UserQueryService;
import com.mayak.ietms.features.user.domain.enums.UserType;
import com.mayak.ietms.request.dto.create.BaseRequestDto;
import com.mayak.ietms.request.dto.create.ContractRequestDto;
import com.mayak.ietms.features.company.domain.model.Company;
import com.mayak.ietms.features.lane.domain.model.Lane;
import com.mayak.ietms.features.location.domain.model.Location;
import com.mayak.ietms.features.request.domain.model.ContractRequest;
import com.mayak.ietms.features.shipment.domain.model.Shipment;
import com.mayak.ietms.request.event.RequestEvent;
import com.mayak.ietms.features.bid.domain.model.Bid;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.domain.enums.RequestStatus;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.shared.exception.business.*;
import com.mayak.ietms.shared.exception.validation.ValidationException;
import com.mayak.ietms.features.location.infra.mapping.LocationMapper;
import com.mayak.ietms.features.bid.infra.persistence.BidRepository;
import com.mayak.ietms.features.lane.infra.persistence.LaneRepository;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.shipment.infra.persistence.ShipmentRepository;
import com.mayak.ietms.features.company.application.CompanyService;
import com.mayak.ietms.features.location.application.LocationCommandService;
import com.mayak.ietms.features.request.application.access.RequestAccessService;
import com.mayak.ietms.features.request.application.bid.RequestBidService;
import com.mayak.ietms.features.request.application.factory.RequestFactory;
import com.mayak.ietms.features.request.application.notify.RequestNotificationService;
import com.mayak.ietms.location.util.LocationParser;
import com.mayak.ietms.request.validator.RequestContractValidator;
import com.mayak.ietms.shipment.event.ShipmentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Orchestrates the full lifecycle of a request —
 * creation, competitor management, bidding, offer, acceptance and deletion.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RequestLifecycleService {

    // repositories
    private final RequestRepository requestRepository;
    private final ShipmentRepository shipmentRepository;
    private final BidRepository bidRepository;
    private final LaneRepository laneRepository;

    // mappers
    private final LocationMapper locationMapper;
    private final RefuseReasonMapper refuseReasonMapper;

    // validators / factories
    private final RequestContractValidator requestContractValidator;
    private final RequestFactory requestFactory;

    // services
    private final UserQueryService userQueryService;
    private final RequestNotificationService requestNotificationService;
    private final ShipmentNotificationService shipmentNotificationService;
    private final RequestAccessService accessService;
    private final RequestBidService bidService;
    private final CompanyService companyService;
    private final RequestLifecycle lifecycle;
    private final LocationCommandService locationCommandService;

    @Transactional
    public Request create(BaseRequestDto dto, Long userId) {
        User actor = userQueryService.getEntityById(userId);
        accessService.requireAuthenticated(actor);
        validate(dto);

        var request = requestFactory.createRequest(dto);
        request.setAuthorId(actor.getId());
        request.setStatus(RequestStatus.NEW);

        Company company = companyService.resolveCompany(dto.getCustomerName());
        request.setCustomer(company);

        List<Long> fromIds = resolveLocationIds(dto.getFromLocations());
        List<Long> toIds   = resolveLocationIds(dto.getToLocations());

        request.setFromLocationIds(fromIds);
        request.setToLocationIds(toIds);

        if (request instanceof ContractRequest contract
                && dto instanceof ContractRequestDto contractDto) {

            Lane lane = laneRepository.findById(contractDto.getLaneId())
                    .orElseThrow(() -> new LaneNotFoundException(contractDto.getLaneId()));

            lifecycle.assignLane(contract, lane);
        }

        Request saved = requestRepository.save(request);
        requestNotificationService.publishEvent(RequestEvent.EventType.CREATED, saved);
        return saved;
    }

    @Transactional
    public void join(Long requestId, Long userId) {
        User actor = userQueryService.getEntityById(userId);
        accessService.requireAuthenticated(actor);

        Request request = getOrThrow(requestId);
        accessService.requireCanJoin(actor, request);
        if (accessService.isJoined(actor, request)) return;
        request.addCompetitor(actor.getId());
        requestRepository.save(request);

        refreshStatus(request);
        requestNotificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
    }

    @Transactional
    public void leave(Long requestId, Long userId) {
        User actor = userQueryService.getEntityById(userId);
        accessService.requireAuthenticated(actor);

        Request request = getOrThrow(requestId);
        if (!accessService.isJoined(actor, request)) return;
        request.removeCompetitor(actor.getId());
        requestRepository.save(request);

        refreshStatus(request);
        requestNotificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
    }

    @Transactional
    public void bid(Long requestId, Long userId) {
        User actor = userQueryService.getEntityById(userId);
        accessService.requireAuthenticated(actor);
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        accessService.requireCanBid(actor, request);
        if (request.getStatus() == RequestStatus.BIDDING) return;
        lifecycle.markBidding(request);
        requestRepository.save(request);
        requestNotificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
    }

    @Transactional
    public void offer(Long requestId, Long userId) {
        User actor = userQueryService.getEntityById(userId);
        accessService.requireAuthenticated(actor);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        if (request.getStatus().isFinal()) {
            throw new RequestStateException(requestId, request.getStatus(), "Operation is not allowed for final request");
        }
        if (!bidService.hasActiveBids(request)) {
            throw new RequestStateException(requestId, request.getStatus(), "Cannot move request to OFFERED without active bids");
        }
        if (request.getStatus() == RequestStatus.OFFERED) return;
        lifecycle.offer(request);

        requestRepository.save(request);
        requestNotificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
    }

    @Transactional
    public void accept(Long requestId, BigDecimal clientPrice, Long userId) {
        User actor = userQueryService.getEntityById(userId);
        accessService.requireAuthenticated(actor);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        if (!bidService.hasActiveBids(request)) {
            throw new RequestStateException(requestId, request.getStatus(), "Cannot accept request without bids");
        }

        Bid bestBid = getBestBid(request);
        if (bestBid.getUser() == null) throw new IllegalStateException("Best bid has no user");

        lifecycle.accept(request, bestBid, clientPrice);
        requestRepository.save(request);

        Shipment shipment = shipmentRepository.findById(request.getId())
                .orElseGet(() -> createShipment(request));
        shipmentNotificationService.publishToParticipants(ShipmentEvent.EventType.STATUS_CHANGED, shipment);

        requestNotificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
        Long dispatcherId = request.getDispatcherId();
        requestNotificationService.publishToUser(dispatcherId, RequestEvent.EventType.UPDATED, request);
    }

    @Transactional
    public void refuse(Long requestId, String reasonCode, Long userId) {
        User actor = userQueryService.getEntityById(userId);
        accessService.requireAuthenticated(actor);

        if (reasonCode == null || reasonCode.isBlank()) {
            throw new RequestStateException(requestId, null, "Refuse reasonCode must be provided");
        }

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        if (request.getStatus().isFinal()) {
            throw new RequestStateException(requestId, request.getStatus(), "Cannot refuse final request");
        }

        RefuseReason reason = refuseReasonMapper.fromCode(reasonCode);
        if (reason == null) {
            throw new RequestStateException(requestId, request.getStatus(), "Invalid refuse reason: " + reasonCode);
        }

        lifecycle.refuse(request, reason);
        requestRepository.save(request);
        requestNotificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
    }

    /**
     * Deletes a request and notifies all participants.
     *
     * <p>Only the request author or an admin may delete a request.
     * Deletion is blocked if a shipment already exists for this request.
     */
    @Transactional
    public void delete(Long requestId, Long userId) {
        User actor = userQueryService.getEntityById(userId);
        accessService.requireAuthenticated(actor);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        boolean isAdmin = actor.getUserType() == UserType.ADMIN;
        if (!request.isAuthoredBy(actor.getId()) && !isAdmin) {
            throw new UnauthorizedException("Only author or admin can delete request");
        }

        if (shipmentRepository.existsByRequestId(requestId)) {
            throw new RequestDeletionNotAllowedException(requestId);
        }

        requestRepository.delete(request);
        requestNotificationService.publishEvent(RequestEvent.EventType.DELETED, request);
    }

    @Transactional
    public void onBidsChanged(Long requestId) {
        Request request = getOrThrow(requestId);
        refreshStatus(request);
        requestNotificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
    }

    @Transactional
    public void updateTid(Long requestId, String tid, Long userId) {
        User actor = userQueryService.getEntityById(userId);
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        if (!request.isAuthoredBy(actor.getId())) {
            throw new UnauthorizedException("Only author can update TID");
        }

        String newTid = (tid == null || tid.isBlank()) ? null : tid.trim();
        if (Objects.equals(request.getTid(), newTid)) {
            return;
        }

        request.setTid(newTid);
        requestRepository.save(request);
        requestNotificationService.publishEvent(RequestEvent.EventType.UPDATED, request);

        shipmentRepository.findById(requestId).ifPresent(shipment ->
                shipmentNotificationService.publishToParticipants(ShipmentEvent.EventType.UPDATED, shipment));
    }

    private void validate(BaseRequestDto dto) {
        var result = requestContractValidator.isValid(dto);
        if (!result.isValid()) throw new ValidationException(result);
    }

    private void refreshStatus(Request request) {
        boolean hasBids = bidService.hasActiveBids(request);
        boolean hasCompetitors = !request.getCompetitorsId().isEmpty();

        RequestStatus before = request.getStatus();
        lifecycle.recalculateStatus(request, hasBids, hasCompetitors);

        if (request.getStatus() != before) {
            requestRepository.save(request);
            requestNotificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
        }
    }

    private Bid getBestBid(Request request) {
        return bidRepository.findByRequestAndDeletedFalse(request).stream()
                .min(Comparator.comparing(Bid::getAmount).thenComparing(Bid::getTime))
                .orElseThrow(() -> new NoActiveBidsException(request.getId()));
    }

    private Request getOrThrow(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException(id));
    }

    private Shipment createShipment(Request request) {
        Shipment shipment = new Shipment(request);
        shipment.assignDispatcher(request.getDispatcherId());
        return shipmentRepository.save(shipment);
    }

    private List<Long> resolveLocationIds(List<String> rawLocations) {
        if (rawLocations == null || rawLocations.isEmpty()) return List.of();

        return rawLocations.stream()
                .map(LocationParser::parse)
                .filter(Objects::nonNull)
                .map(locationMapper::toEntity)
                .map(locationCommandService::resolve)
                .map(Location::getId)
                .toList();
    }
}