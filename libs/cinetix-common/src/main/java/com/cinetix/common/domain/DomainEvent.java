package com.cinetix.common.domain;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID getEventId();
    String getEventType();
    String getAggregateType();
    Object getAggregateId();
    Instant getOccurredAt();
    default String getVersion() { return "v1"; }
}