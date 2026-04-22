package com.cinetix.booking.domain.model;

import com.cinetix.booking.domain.model.valueobject.SeatId;
import com.cinetix.common.domain.valueobject.Money;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "booking_items",
    uniqueConstraints = @UniqueConstraint(columnNames = {"booking_id", "seat_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "seat_id", nullable = false)
    private UUID seatId;

    @Column(name = "seat_code", nullable = false, length = 20)
    private String seatCode;

    @Column(name = "seat_category", nullable = false, length = 20)
    private String seatCategory;

    @Column(name = "unit_price", nullable = false)
    private long unitPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static BookingItem of(Booking booking, SeatId seatId, String seatCode,
                                  String category, Money price) {
        var item = new BookingItem();
        item.booking = booking;
        item.seatId = seatId.value();
        item.seatCode = seatCode;
        item.seatCategory = category;
        item.unitPrice = price.toLong();
        item.createdAt = Instant.now();
        return item;
    }

    public SeatId getSeatId() { return SeatId.of(seatId); }
    public Money getPrice()   { return Money.ofVnd(unitPrice); }
}
