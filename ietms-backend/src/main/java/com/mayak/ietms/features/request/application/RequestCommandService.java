package com.mayak.ietms.features.request.application;

import com.mayak.ietms.features.request.application.lifecycle.*;
import com.mayak.ietms.request.dto.create.BaseRequestDto;
import com.mayak.ietms.features.request.domain.model.Request;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RequestCommandService {

    private final CreateRequestUseCase createRequestUseCase;
    private final JoinRequestUseCase joinRequestUseCase;
    private final LeaveRequestUseCase leaveRequestUseCase;
    private final OfferRequestUseCase offerRequestUseCase;
    private final AcceptRequestUseCase acceptRequestUseCase;
    private final RefuseRequestUseCase refuseRequestUseCase;
    private final ExpireRequestUseCase expireRequestUseCase;
    private final DeleteRequestUseCase deleteRequestUseCase;
    private final UpdateTidUseCase updateTidUseCase;

    public Request create(BaseRequestDto dto, Long userId) {
        return createRequestUseCase.execute(dto, userId);
    }

    public void join(long requestId, Long userId) {
        joinRequestUseCase.execute(requestId, userId);
    }

    public void leave(long requestId, Long userId) {
        leaveRequestUseCase.execute(requestId, userId);
    }

    public void offer(long requestId, Long userId) {
        offerRequestUseCase.execute(requestId, userId);
    }

    public void accept(long requestId, BigDecimal price, Long userId) {
        acceptRequestUseCase.execute(requestId, price, userId);
    }

    public void refuse(long requestId, String reason, Long userId) {
        refuseRequestUseCase.execute(requestId, reason, userId);
    }

    public void updateTid(long requestId, String tid, Long userId) {
        updateTidUseCase.execute(requestId, tid, userId);
    }

    public void delete(long requestId, Long userId) {
        deleteRequestUseCase.execute(requestId, userId);
    }

    public void expire(long requestId, Long userId) {
        expireRequestUseCase.execute(requestId, userId);
    }

}