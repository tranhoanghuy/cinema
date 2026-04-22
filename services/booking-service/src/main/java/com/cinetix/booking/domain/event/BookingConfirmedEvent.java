package com.cinetix.booking.domain.event;

import com.cinetix.booking.domain.model.valueobject.*;
import com.cinetix.common.domain.DomainEvent;
import com.cinetix.common.domain.valueobject.Money;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BookingConfirmedEvent(
    UUID eventId, BookingId aggregateId,
    CustomerId customerId, ShowtimeId showtimeId,
    List<SeatId> seatIds, Money finalAmount,
    VoucherCode appliedVoucher, String paymentMethod,
    Instant occurredAt
) implements DomainEvent {
    @Override public String getEventType()     { return "booking.events.confirmed.v1"; }
    @Override public String getAggregateType() { return "Booking"; }
    @Override public Object getAggregateId()   { return aggregateId; }
    @Override public UUID   getEventId()       { return eventId; }
    @Override public Instant getOccurredAt()   { return occurredAt; }
}
