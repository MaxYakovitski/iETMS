package com.mayak.iet.shared.exception.business;

import com.mayak.iet.features.request.domain.enums.RequestStatus;
import lombok.Getter;

@Getter
public class RequestStateException extends RuntimeException {

    private final Long requestId;
    private final RequestStatus status;

    public RequestStateException(Long requestId, RequestStatus status, String message) {
        super(message);
        this.requestId = requestId;
        this.status = status;
    }
}