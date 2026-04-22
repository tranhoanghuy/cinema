package com.cinetix.ticket.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "seat_id", nullable = false)
    private UUID seatId;

    @Column(name = "seat_code", nullable = false, length = 20)
    private String seatCode;

    @Column(name = "showtime_id", nullable = false)
    private UUID showtimeId;

    @Column(name = "movie_title", nullable = false, length = 500)
    private String movieTitle;

    @Column(name = "cinema_name", nullable = false, length = 300)
    private String cinemaName;

    @Column(name = "screen_name", nullable = false, length = 100)
    private String screenName;

    @Column(name = "showtime_start", nullable = false)
    private Instant showtimeStart;

    @Column(name = "unit_price", nullable = false)
    private long unitPrice;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;

    @Column(name = "qr_data", length = 500)
    private String qrData;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TicketStatus status = TicketStatus.ACTIVE;

    @Column(name = "issued_at", nullable = false)
    @Builder.Default
    private Instant issuedAt = Instant.now();

    @Column(name = "used_at")
    private Instant usedAt;

    public void markUsed() {
        this.status = TicketStatus.USED;
        this.usedAt = Instant.now();
    }

    public void cancel() {
        this.status = TicketStatus.CANCELLED;
    }
}
