package com.ticketing.common.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public abstract class DomainEvent {

    private final String eventId;
    private final String eventType;
    private final Instant occurredAt;

    protected DomainEvent(String eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.occurredAt = Instant.now();
    }

}