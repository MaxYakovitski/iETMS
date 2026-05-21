package com.mayak.ietms.features.request.application;

import com.mayak.ietms.features.request.application.bid.RequestBidService;
import com.mayak.ietms.features.request.domain.enums.RequestStatus;
import com.mayak.ietms.features.request.domain.lifecycle.RequestLifecycle;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Shared helper that recalculates and persists the request status
 * after competitor or bid changes.
 */
@Service
@RequiredArgsConstructor
public class RequestStatusService {

    private final RequestRepository requestRepository;
    private final RequestBidService bidService;
    private final RequestLifecycle lifecycle;

    public void refresh(Request request) {
        boolean hasBids = bidService.hasActiveBids(request);
        boolean hasCompetitors = !request.getCompetitorsId().isEmpty();

        RequestStatus before = request.getStatus();
        lifecycle.recalculateStatus(request, hasBids, hasCompetitors);

        if (request.getStatus() != before) {
            requestRepository.save(request);
        }
    }
}