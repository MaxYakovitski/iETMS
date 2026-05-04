package com.mayak.ietms.features.request.application.scheduler;

import com.mayak.ietms.features.request.domain.enums.ReasonCode;
import com.mayak.ietms.features.request.domain.enums.RequestStatus;
import com.mayak.ietms.features.request.domain.lifecycle.RequestLifecycle;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.scheduler.SchedulerMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestSchedulerService {

    private final RequestRepository requestRepository;
    private final RequestLifecycle lifecycle;

    @Transactional
    public void processExpiredRequests(SchedulerMode mode) {
        autoRefuseExpiredRequests(mode);
        autoArchiveOldRequests(mode);
    }

    private void autoRefuseExpiredRequests(SchedulerMode mode) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(3);

        List<Request> expired = requestRepository.findExpiredRequests(
                Set.of(RequestStatus.NEW, RequestStatus.IN_PROGRESS),
                threshold);

        for (Request r : expired) {
            if (mode == SchedulerMode.DRY_RUN) {
                log.info("[DRY-RUN] Would auto-refuse request {}", r.getId());
            } else {
                lifecycle.refuse(r, ReasonCode.BID_NOT_PROVIDED);
                requestRepository.save(r);
            }
        }

        log.info("Auto-refuse processed {} requests (mode={})", expired.size(), mode);
    }

    private void autoArchiveOldRequests(SchedulerMode mode) {
        Instant threshold = Instant.now().minus(45, ChronoUnit.DAYS);

        List<Request> toArchive = requestRepository.findRequestsForArchiving(
                Set.of(RequestStatus.ACCEPTED, RequestStatus.REFUSED),
                threshold);

        for (Request r : toArchive) {
            if (mode == SchedulerMode.DRY_RUN) {
                log.info("[DRY-RUN] Would archive request {}", r.getId());
            } else {
                lifecycle.archive(r);
                requestRepository.save(r);
            }
        }

        log.info("Auto-archive processed {} requests (mode={})", toArchive.size(), mode);
    }
}