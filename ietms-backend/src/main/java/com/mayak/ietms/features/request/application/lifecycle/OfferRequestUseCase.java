package com.mayak.ietms.features.request.application.lifecycle;

import com.mayak.ietms.features.request.application.access.RequestAccessService;
import com.mayak.ietms.features.request.application.bid.RequestBidService;
import com.mayak.ietms.features.request.application.notify.RequestNotificationService;
import com.mayak.ietms.features.request.domain.enums.RequestStatus;
import com.mayak.ietms.features.request.domain.lifecycle.RequestLifecycle;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.user.application.UserQueryService;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.request.event.RequestEvent;
import com.mayak.ietms.shared.exception.business.RequestNotFoundException;
import com.mayak.ietms.shared.exception.business.RequestStateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OfferRequestUseCase {

    private final RequestRepository requestRepository;
    private final UserQueryService userQueryService;
    private final RequestNotificationService requestNotificationService;
    private final RequestAccessService accessService;
    private final RequestBidService bidService;
    private final RequestLifecycle lifecycle;

    public void execute(Long requestId, Long userId) {
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
}