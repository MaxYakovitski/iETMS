package com.mayak.ietms.features.request.application.lifecycle;

import com.mayak.ietms.features.request.application.RequestStatusService;
import com.mayak.ietms.features.request.application.notify.RequestNotificationService;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.request.event.RequestEvent;
import com.mayak.ietms.shared.exception.business.RequestNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OnBidsChangedUseCase {

    private final RequestRepository requestRepository;
    private final RequestNotificationService requestNotificationService;
    private final RequestStatusService statusRefreshService;

    public void execute(Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));
        statusRefreshService.refresh(request);
        requestNotificationService.publishEvent(RequestEvent.EventType.UPDATED, request);
    }
}