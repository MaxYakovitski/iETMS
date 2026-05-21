package com.mayak.ietms.features.request.application.lifecycle;

import com.mayak.ietms.features.request.application.notify.RequestNotificationService;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.shipment.application.notify.ShipmentNotificationService;
import com.mayak.ietms.features.shipment.infra.persistence.ShipmentRepository;
import com.mayak.ietms.features.user.application.UserQueryService;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.request.event.RequestEvent;
import com.mayak.ietms.shared.exception.business.RequestNotFoundException;
import com.mayak.ietms.shared.exception.business.UnauthorizedException;
import com.mayak.ietms.shipment.event.ShipmentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateTidUseCase {

    private final RequestRepository requestRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserQueryService userQueryService;
    private final RequestNotificationService requestNotificationService;
    private final ShipmentNotificationService shipmentNotificationService;

    public void execute(Long requestId, String tid, Long userId) {
        User actor = userQueryService.getEntityById(userId);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        if (!request.isAuthoredBy(actor.getId())) {
            throw new UnauthorizedException("Only author can update TID");
        }

        String newTid = (tid == null || tid.isBlank()) ? null : tid.trim();
        if (Objects.equals(request.getTid(), newTid)) return;

        request.setTid(newTid);
        requestRepository.save(request);
        requestNotificationService.publishEvent(RequestEvent.EventType.UPDATED, request);

        shipmentRepository.findById(requestId).ifPresent(shipment ->
                shipmentNotificationService.publishToParticipants(ShipmentEvent.EventType.UPDATED, shipment));
    }

}