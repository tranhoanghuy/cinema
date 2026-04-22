package com.cinetix.showtime.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seat_status",
    uniqueConstraints = @UniqueConstraint(columnNames = {"showtime_id", "seat_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatStatusEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "showtime_id", nullable = false)
    private UUID showtimeId;

    @Column(name = "seat_id", nullable = false)
    private UUID seatId;

    @Column(name = "seat_code", nullable = false, length = 20)
    private String seatCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private SeatCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SeatOccupancyStatus status;

    @Column(name = "booking_id")
    private UUID bookingId;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "unit_price", nullable = false)
    private long unitPrice;

    public void confirm(UUID bookingId) {
        this.status = SeatOccupancyStatus.BOOKED;
        this.bookingId = bookingId;
        this.confirmedAt = Instant.now();
    }

    public boolean isAvailableInDb() {
        return status == SeatOccupancyStatus.AVAILABLE;
    }
}
