package com.mayak.ietms.features.request.application.lifecycle;

import com.mayak.ietms.features.request.application.access.RequestAccessService;
import com.mayak.ietms.features.request.application.notify.RequestNotificationService;
import com.mayak.ietms.features.request.domain.enums.ReasonCode;
import com.mayak.ietms.features.request.domain.lifecycle.RequestLifecycle;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.user.application.UserQueryService;
import com.mayak.ietms.features.user.domain.enums.UserType;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.request.event.RequestEvent;
import com.mayak.ietms.shared.exception.business.RequestNotFoundException;
import com.mayak.ietms.shared.exception.business.RequestStateException;
import com.mayak.ietms.shared.exception.business.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpireRequestUseCase {

    private final RequestRepository requestRepository;
    private final UserQueryService userQueryService;
    private final RequestNotificationService requestNotificationService;
    private final RequestLifecycle lifecycle;
    private final RequestAccessService accessService;

    public void execute(Long requestId, Long userId) {
        User actor = userQueryService.getEntityById(userId);
        accessService.requireAuthenticated(actor);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        boolean isAdmin = actor.getUserType() == UserType.ADMIN;
        if (!request.isAuthoredBy(actor.getId()) && !isAdmin) {
            throw new UnauthorizedException("Only author or admin can expire request");
        }

        if (request.getStatus().isFinal()) {
            throw new RequestStateException(requestId, request.getStatus(),
                    "Cannot expire request in final state");
        }

        lifecycle.refuse(request, ReasonCode.BID_NOT_PROVIDED);
        requestRepository.save(request);
        requestNotificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
    }
}