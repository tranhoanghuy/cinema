package com.cinetix.booking.domain.event;

import com.cinetix.booking.domain.model.valueobject.BookingId;
import com.cinetix.common.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record BookingFailedEvent(
    UUID eventId, BookingId aggregateId, String reason, Instant occurredAt
) implements DomainEvent {
    @Override public String getEventType()     { return "booking.events.failed.v1"; }
    @Override public String getAggregateType() { return "Booking"; }
    @Override public Object getAggregateId()   { return aggregateId; }
    @Override public UUID   getEventId()       { return eventId; }
    @Override public Instant getOccurredAt()   { return occurredAt; }
}
