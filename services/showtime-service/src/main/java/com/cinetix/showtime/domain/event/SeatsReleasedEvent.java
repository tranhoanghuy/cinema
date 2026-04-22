package com.cinetix.showtime.domain.event;

import com.cinetix.common.domain.DomainEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SeatsReleasedEvent(
    UUID eventId, UUID showtimeId, List<String> seatIds,
    UUID bookingId, String reason, Instant occurredAt
) implements DomainEvent {
    @Override public String getEventType()     { return "showtime.events.seats_released.v1"; }
    @Override public String getAggregateType() { return "Showtime"; }
    @Override public Object getAggregateId()   { return showtimeId; }
    @Override public UUID   getEventId()       { return eventId; }
    @Override public Instant getOccurredAt()   { return occurredAt; }
}
