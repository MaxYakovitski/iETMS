package com.mayak.iet.features.request.application.lifecycle;

import com.mayak.iet.features.request.domain.enums.ReasonCode;
import com.mayak.iet.features.request.domain.lifecycle.RequestLifecycle;
import com.mayak.iet.features.request.domain.model.RefuseReason;
import com.mayak.iet.features.request.infra.mapping.RefuseReasonMapper;
import com.mayak.iet.features.user.application.UserQueryService;
import com.mayak.iet.request.dto.create.BaseRequestDto;
import com.mayak.iet.request.dto.create.ContractRequestDto;
import com.mayak.iet.features.company.domain.model.Company;
import com.mayak.iet.features.lane.domain.model.Lane;
import com.mayak.iet.features.location.domain.model.Location;
import com.mayak.iet.features.request.domain.model.ContractRequest;
import com.mayak.iet.features.shipment.domain.model.Shipment;
import com.mayak.iet.request.event.RequestEvent;
import com.mayak.iet.features.bid.domain.model.Bid;
import com.mayak.iet.features.request.domain.model.Request;
import com.mayak.iet.features.request.domain.enums.RequestStatus;
import com.mayak.iet.features.user.domain.model.User;
import com.mayak.iet.scheduler.SchedulerMode;
import com.mayak.iet.shared.exception.business.*;
import com.mayak.iet.shared.exception.validation.ValidationException;
import com.mayak.iet.features.location.infra.mapping.LocationMapper;
import com.mayak.iet.features.bid.infra.persistence.BidRepository;
import com.mayak.iet.features.lane.infra.persistence.LaneRepository;
import com.mayak.iet.features.request.infra.persistence.RequestRepository;
import com.mayak.iet.features.shipment.infra.persistence.ShipmentRepository;
import com.mayak.iet.features.company.application.CompanyService;
import com.mayak.iet.features.location.application.LocationCommandService;
import com.mayak.iet.features.request.application.access.RequestAccessService;
import com.mayak.iet.features.request.application.bid.RequestBidService;
import com.mayak.iet.features.request.application.factory.RequestFactory;
import com.mayak.iet.features.request.application.notify.RequestNotificationService;
import com.mayak.iet.location.util.LocationParser;
import com.mayak.iet.request.validator.RequestContractValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestLifecycleService {

    private final RequestRepository requestRepository;
    private final ShipmentRepository shipmentRepository;
    private final BidRepository bidRepository;
    private final LaneRepository laneRepository;
    private final RequestContractValidator requestContractValidator;
    private final RequestFactory requestFactory;
    private final UserQueryService userQueryService;
    private final RequestNotificationService notificationService;
    private final RequestAccessService accessService;
    private final RequestBidService bidService;
    private final CompanyService companyService;
    private final RequestLifecycle lifecycle;
    private final LocationCommandService locationCommandService;
    private final LocationMapper locationMapper;
    private final RefuseReasonMapper refuseReasonMapper;

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
        notificationService.publishEvent(RequestEvent.EventType.CREATED, saved);
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
        notificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
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
        notificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
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
        notificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
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
        notificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
    }

    @Transactional
    public void accept(Long requestId, BigDecimal clientPrice, Long userId) {
        User actor = userQueryService.getEntityById(userId);
        accessService.requireAuthenticated(actor);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        if (request.getStatus().isFinal()) {
            throw new RequestStateException(requestId, request.getStatus(), "Cannot accept final request");
        }
        if (request.getStatus() != RequestStatus.OFFERED) {
            throw new RequestStateException(requestId, request.getStatus(), "Request must be OFFERED before acceptance");
        }
        if (!bidService.hasActiveBids(request)) {
            throw new RequestStateException(requestId, request.getStatus(), "Cannot accept request without bids");
        }

        Bid bestBid = getBestBid(request);
        Long bidderId = bestBid.getUser() != null ? bestBid.getUser().getId() : null;
        if (bidderId == null) throw new IllegalStateException("Best bid has no user");

        lifecycle.accept(request, bestBid, clientPrice);
        requestRepository.save(request);

        shipmentRepository.findById(request.getId())
                .orElseGet(() -> createShipment(request));

        notificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
        notificationService.publishToUser(userId, RequestEvent.EventType.UPDATED, request);
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
        notificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
    }

    @Transactional
    public void delete(Long requestId, Long userId) {
        User actor = userQueryService.getEntityById(userId);
        accessService.requireAuthenticated(actor);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        if (!request.isAuthoredBy(actor.getId())) {
            throw new UnauthorizedException("Only author can delete request");
        }

        if (shipmentRepository.existsByRequestId(requestId)) {
            throw new RequestDeletionNotAllowedException(requestId);
        }

        requestRepository.delete(request);
        notificationService.publishEvent(RequestEvent.EventType.DELETED, request);
    }

    @Transactional
    public void onBidsChanged(Long requestId) {
        Request request = getOrThrow(requestId);
        refreshStatus(request);
        notificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
    }

    @Transactional
    public void updateTid(Long requestId, String tid, Long userId) {
        User actor = userQueryService.getEntityById(userId);

        if (tid == null || tid.isBlank()) {
            throw new RequestStateException(requestId, null, "TID must not be empty");
        }

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        if (!request.isAuthoredBy(actor.getId())) {
            throw new UnauthorizedException("Only author can update TID");
        }

        request.setTid(tid);
        requestRepository.save(request);
        notificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
    }



    @Transactional
    public void autoRefuseExpiredRequests(SchedulerMode mode) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(3);

        List<Request> expired = requestRepository.findExpiredRequests(
                Set.of(RequestStatus.NEW, RequestStatus.IN_PROGRESS),
                threshold
        );

        for (Request r : expired) {
            if (mode == SchedulerMode.DRY_RUN) {
                log.info("[DRY-RUN] Would auto-refuse request {}", r.getId());
            } else {
                lifecycle.refuse(r, ReasonCode.BID_NOT_PROVIDED);
            }
        }

        log.info("Auto-refuse processed {} requests (mode={})", expired.size(), mode);
    }

    @Transactional
    public void autoArchiveOldRequests(SchedulerMode mode) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(45);

        List<Request> toArchive = requestRepository.findRequestsForArchiving(
                Set.of(RequestStatus.ACCEPTED, RequestStatus.REFUSED),
                threshold
        );

        for (Request r : toArchive) {
            if (mode == SchedulerMode.DRY_RUN) {
                log.info("[DRY-RUN] Would archive request {}", r.getId());
            } else {
                lifecycle.archive(r);
            }
        }

        log.info("Auto-archive processed {} requests (mode={})", toArchive.size(), mode);
    }

    @Transactional
    public void processExpiredRequests(SchedulerMode mode) {
        autoRefuseExpiredRequests(mode);
        autoArchiveOldRequests(mode);
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
            notificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
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