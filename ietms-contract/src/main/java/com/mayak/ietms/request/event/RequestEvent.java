package com.mayak.ietms.request.event;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor(force = true)
public class RequestEvent <T> {
    public enum EventType {
        CREATED,
        UPDATED,
        DELETED
    }

    private final Long requestId;
    private final EventType type;
    private final long version;
    private final T payload;
    private final Instant occurredAt;

    public RequestEvent(Long requestId, EventType type, long version, T payload) {
        this.requestId = requestId;
        this.type = type;
        this.version = version;
        this.payload = payload;
        this.occurredAt = Instant.now();
    }
}