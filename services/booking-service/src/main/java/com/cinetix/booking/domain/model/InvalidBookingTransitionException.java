package com.cinetix.booking.domain.model;

import com.cinetix.booking.domain.model.valueobject.BookingId;
import com.cinetix.common.exception.BusinessException;

public class InvalidBookingTransitionException extends BusinessException {
    public InvalidBookingTransitionException(BookingId id, BookingStatus from, BookingStatus to) {
        super("INVALID_BOOKING_TRANSITION",
            String.format("Booking %s cannot transition from %s to %s", id, from, to));
    }
}
