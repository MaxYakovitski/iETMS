package com.mayak.ietms.company.event;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor(force = true)
public class CompanyEvent <T>{

    public enum EventType {
        CREATED,
        UPDATED,
        DELETED
    }

    private final Long companyId;
    private final EventType type;
    private final long version;
    private final T payload;
    private final Instant occurredAt;

    public CompanyEvent(Long companyId, EventType type, long version, T payload) {
        this.companyId = companyId;
        this.type = type;
        this.version = version;
        this.payload = payload;
        this.occurredAt = Instant.now();
    }
}