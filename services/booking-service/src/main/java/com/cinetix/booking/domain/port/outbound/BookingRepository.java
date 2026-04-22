package com.cinetix.booking.domain.port.outbound;

import com.cinetix.booking.domain.model.Booking;
import com.cinetix.booking.domain.model.BookingStatus;
import com.cinetix.booking.domain.model.valueobject.BookingId;
import com.cinetix.booking.domain.model.valueobject.CustomerId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findById(BookingId id);
    List<Booking> findExpiredPendingBookings(Instant now, int limit);
    List<Booking> findByCustomerId(CustomerId customerId, String status, int page, int size);
    long countByCustomerId(CustomerId customerId, String status);
}
