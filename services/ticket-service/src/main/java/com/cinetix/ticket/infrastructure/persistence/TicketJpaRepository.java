package com.cinetix.ticket.infrastructure.persistence;

import com.cinetix.ticket.domain.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketJpaRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByBookingId(UUID bookingId);
    List<Ticket> findByCustomerIdOrderByIssuedAtDesc(UUID customerId);
    List<Ticket> findBySeatIdAndShowtimeId(UUID seatId, UUID showtimeId);
}
