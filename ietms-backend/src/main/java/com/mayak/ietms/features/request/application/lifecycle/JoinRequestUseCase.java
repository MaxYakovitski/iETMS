package com.mayak.ietms.features.request.application.lifecycle;

import com.mayak.ietms.features.request.application.RequestStatusService;
import com.mayak.ietms.features.request.application.access.RequestAccessService;
import com.mayak.ietms.features.request.application.notify.RequestNotificationService;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.user.application.UserQueryService;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.request.event.RequestEvent;
import com.mayak.ietms.shared.exception.business.AlreadyJoinedException;
import com.mayak.ietms.shared.exception.business.RequestNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class JoinRequestUseCase {

    private final RequestRepository requestRepository;
    private final UserQueryService userQueryService;
    private final RequestNotificationService requestNotificationService;
    private final RequestAccessService accessService;
    private final RequestStatusService statusRefreshService;

    public void execute(Long requestId, Long userId) {
        User actor = userQueryService.getEntityById(userId);
        accessService.requireAuthenticated(actor);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        if (accessService.isJoined(actor, request)) throw new AlreadyJoinedException(requestId);
        accessService.requireCanJoin(actor, request);

        request.addCompetitor(actor.getId());
        requestRepository.save(request);

        statusRefreshService.refresh(request);
        requestNotificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
    }

}