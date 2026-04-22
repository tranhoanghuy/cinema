package com.cinetix.booking.infrastructure.persistence;

import com.cinetix.booking.domain.model.Booking;
import com.cinetix.booking.domain.model.BookingStatus;
import com.cinetix.booking.domain.model.valueobject.BookingId;
import com.cinetix.booking.domain.model.valueobject.CustomerId;
import com.cinetix.booking.domain.port.outbound.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BookingRepositoryAdapter implements BookingRepository {

    private final BookingJpaRepository jpaRepository;

    @Override
    public Booking save(Booking booking) {
        return jpaRepository.save(booking);
    }

    @Override
    public Optional<Booking> findById(BookingId id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Booking> findExpiredPendingBookings(Instant now, int limit) {
        return jpaRepository.findExpiredPending(now, PageRequest.of(0, limit));
    }

    @Override
    public List<Booking> findByCustomerId(CustomerId customerId, String status, int page, int size) {
        if (status != null && !status.isBlank()) {
            return jpaRepository.findByCustomerIdAndStatus(
                customerId, BookingStatus.valueOf(status), PageRequest.of(page, size));
        }
        return jpaRepository.findByCustomerId(customerId, PageRequest.of(page, size));
    }

    @Override
    public long countByCustomerId(CustomerId customerId, String status) {
        if (status != null && !status.isBlank()) {
            return jpaRepository.countByCustomerIdAndStatus(
                customerId, BookingStatus.valueOf(status));
        }
        return jpaRepository.countByCustomerId(customerId);
    }
}
