package com.mayak.ietms.features.request.application;

import com.mayak.ietms.request.dto.create.BaseRequestDto;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.application.lifecycle.RequestLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Application-layer facade for request write operations.
 * Delegates all business logic to {@link RequestLifecycleService}.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RequestCommandService {

    private final RequestLifecycleService lifecycleService;

    public Request create(BaseRequestDto dto, Long userId) {
        return lifecycleService.create(dto, userId);
    }

    public void join(long requestId, Long userId) {
        lifecycleService.join(requestId, userId);
    }

    public void leave(long requestId, Long userId) {
        lifecycleService.leave(requestId, userId);
    }

    public void bid(long requestId, Long userId) {
        lifecycleService.bid(requestId, userId);
    }

    public void offer(long requestId, Long userId) {
        lifecycleService.offer(requestId, userId);
    }

    public void accept(long requestId, BigDecimal price, Long userId) {
        lifecycleService.accept(requestId, price, userId);
    }

    public void refuse(long requestId, String reason, Long userId) {
        lifecycleService.refuse(requestId, reason, userId);
    }

    public void updateTid(long requestId, String tid, Long userId) {
        lifecycleService.updateTid(requestId, tid, userId);
    }

    public void delete(long requestId, Long userId) {
        lifecycleService.delete(requestId, userId);
    }

    public void expire(long requestId, Long userId) {
        lifecycleService.expire(requestId, userId);
    }

}