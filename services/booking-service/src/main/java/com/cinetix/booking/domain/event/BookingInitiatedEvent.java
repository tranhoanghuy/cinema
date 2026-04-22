package com.cinetix.booking.domain.event;

import com.cinetix.booking.domain.model.valueobject.*;
import com.cinetix.common.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record BookingInitiatedEvent(
    UUID eventId, BookingId aggregateId,
    CustomerId customerId, ShowtimeId showtimeId,
    Instant occurredAt
) implements DomainEvent {
    @Override public String getEventType()     { return "booking.events.initiated.v1"; }
    @Override public String getAggregateType() { return "Booking"; }
    @Override public Object getAggregateId()   { return aggregateId; }
    @Override public UUID   getEventId()       { return eventId; }
    @Override public Instant getOccurredAt()   { return occurredAt; }
}
