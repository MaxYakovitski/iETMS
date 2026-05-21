package com.mayak.ietms.features.request.application.lifecycle;

import com.mayak.ietms.features.request.application.access.RequestAccessService;
import com.mayak.ietms.features.request.application.notify.RequestNotificationService;
import com.mayak.ietms.features.request.domain.lifecycle.RequestLifecycle;
import com.mayak.ietms.features.request.domain.model.RefuseReason;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.infra.mapping.RefuseReasonMapper;
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
public class RefuseRequestUseCase {

    private final RequestRepository requestRepository;
    private final RefuseReasonMapper refuseReasonMapper;
    private final UserQueryService userQueryService;
    private final RequestNotificationService requestNotificationService;
    private final RequestAccessService accessService;
    private final RequestLifecycle lifecycle;

    public void execute(Long requestId, String reasonCode, Long userId) {
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
}