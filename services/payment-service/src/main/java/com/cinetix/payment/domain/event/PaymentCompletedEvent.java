package com.cinetix.payment.domain.event;

import com.cinetix.common.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record PaymentCompletedEvent(
    UUID eventId, UUID paymentId, UUID bookingId,
    UUID customerId, long amount, String currency,
    String method, Instant occurredAt
) implements DomainEvent {
    @Override public String getEventType()     { return "payment.events.completed.v1"; }
    @Override public String getAggregateType() { return "Payment"; }
    @Override public Object getAggregateId()   { return paymentId; }
    @Override public UUID   getEventId()       { return eventId; }
    @Override public Instant getOccurredAt()   { return occurredAt; }
}
