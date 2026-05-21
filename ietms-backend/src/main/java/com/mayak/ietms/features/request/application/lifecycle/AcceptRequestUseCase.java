package com.mayak.ietms.features.request.application.lifecycle;

import com.mayak.ietms.features.bid.domain.model.Bid;
import com.mayak.ietms.features.bid.infra.persistence.BidRepository;
import com.mayak.ietms.features.request.application.access.RequestAccessService;
import com.mayak.ietms.features.request.application.bid.RequestBidService;
import com.mayak.ietms.features.request.application.notify.RequestNotificationService;
import com.mayak.ietms.features.request.domain.lifecycle.RequestLifecycle;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.shipment.application.notify.ShipmentNotificationService;
import com.mayak.ietms.features.shipment.domain.model.Shipment;
import com.mayak.ietms.features.shipment.infra.persistence.ShipmentRepository;
import com.mayak.ietms.features.user.application.UserQueryService;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.request.event.RequestEvent;
import com.mayak.ietms.shared.exception.business.NoActiveBidsException;
import com.mayak.ietms.shared.exception.business.RequestNotFoundException;
import com.mayak.ietms.shared.exception.business.RequestStateException;
import com.mayak.ietms.shipment.event.ShipmentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
@Transactional
public class AcceptRequestUseCase {

    private final RequestRepository requestRepository;
    private final ShipmentRepository shipmentRepository;
    private final BidRepository bidRepository;
    private final UserQueryService userQueryService;
    private final RequestNotificationService requestNotificationService;
    private final ShipmentNotificationService shipmentNotificationService;
    private final RequestAccessService accessService;
    private final RequestBidService bidService;
    private final RequestLifecycle lifecycle;

    public void execute(Long requestId, BigDecimal clientPrice, Long userId) {
        User actor = userQueryService.getEntityById(userId);
        accessService.requireAuthenticated(actor);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        if (!bidService.hasActiveBids(request)) {
            throw new RequestStateException(requestId, request.getStatus(), "Cannot accept request without bids");
        }

        Bid bestBid = bidRepository.findByRequestAndDeletedFalse(request).stream()
                .min(Comparator.comparing(Bid::getAmount).thenComparing(Bid::getTime))
                .orElseThrow(() -> new NoActiveBidsException(request.getId()));

        if (bestBid.getUser() == null) throw new IllegalStateException("Best bid has no user");

        lifecycle.accept(request, bestBid, clientPrice);
        requestRepository.save(request);

        var dispatcherId = request.getDispatcherId();
        Shipment shipment = shipmentRepository.findById(request.getId())
                .orElseGet(() -> {
                    Shipment s = new Shipment(request);
                    s.assignDispatcher(dispatcherId);
                    return shipmentRepository.save(s);
                });

        shipmentNotificationService.publishToParticipants(ShipmentEvent.EventType.STATUS_CHANGED, shipment);
        requestNotificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
        requestNotificationService.publishToUser(dispatcherId, RequestEvent.EventType.UPDATED, request);
    }
}