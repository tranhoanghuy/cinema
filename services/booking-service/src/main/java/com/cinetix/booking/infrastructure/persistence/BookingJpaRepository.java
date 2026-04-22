package com.cinetix.booking.infrastructure.persistence;

import com.cinetix.booking.domain.model.Booking;
import com.cinetix.booking.domain.model.BookingStatus;
import com.cinetix.booking.domain.model.valueobject.BookingId;
import com.cinetix.booking.domain.model.valueobject.CustomerId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface BookingJpaRepository extends JpaRepository<Booking, BookingId> {

    @Query("SELECT b FROM Booking b WHERE b.status = 'PAYMENT_PENDING' AND b.expiresAt < :now")
    List<Booking> findExpiredPending(@Param("now") Instant now, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.customerId = :cid ORDER BY b.createdAt DESC")
    List<Booking> findByCustomerId(@Param("cid") CustomerId customerId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.customerId = :cid AND b.status = :status ORDER BY b.createdAt DESC")
    List<Booking> findByCustomerIdAndStatus(@Param("cid") CustomerId customerId,
                                             @Param("status") BookingStatus status,
                                             Pageable pageable);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.customerId = :cid")
    long countByCustomerId(@Param("cid") CustomerId customerId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.customerId = :cid AND b.status = :status")
    long countByCustomerIdAndStatus(@Param("cid") CustomerId customerId,
                                     @Param("status") BookingStatus status);
}
