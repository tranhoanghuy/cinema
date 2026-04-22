package com.cinetix.booking.domain.model;

import com.cinetix.booking.domain.event.*;
import com.cinetix.booking.domain.model.valueobject.*;
import com.cinetix.common.domain.AggregateRoot;
import com.cinetix.common.domain.valueobject.Money;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "bookings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking extends AggregateRoot<BookingId> {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "id"))
    private BookingId id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "customer_id", nullable = false))
    private CustomerId customerId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "showtime_id", nullable = false))
    private ShowtimeId showtimeId;

    @Column(name = "cinema_id", nullable = false)
    private UUID cinemaId;

    @Column(name = "movie_id", nullable = false)
    private UUID movieId;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("seatCode ASC")
    private List<BookingItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private BookingStatus status;

    @Column(name = "subtotal", nullable = false)
    private long subtotalValue;

    @Column(name = "discount_amount", nullable = false)
    private long discountAmountValue = 0L;

    @Column(name = "final_amount", nullable = false)
    private long finalAmountValue;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "VND";

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "voucher_code"))
    private VoucherCode appliedVoucher;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "payment_id"))
    private PaymentId paymentId;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(name = "payment_url")
    private String paymentUrl;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    private static final int MAX_SEATS = 8;
    private static final Duration TTL   = Duration.ofMinutes(10);

    // ── Factory ──────────────────────────────────────────────────────────────
    public static Booking initiate(CustomerId customerId, ShowtimeId showtimeId,
                                    UUID cinemaId, UUID movieId,
                                    List<SeatSelection> selections, String paymentMethod,
                                    Clock clock) {
        Objects.requireNonNull(customerId);
        Objects.requireNonNull(showtimeId);
        if (selections == null || selections.isEmpty())
            throw new IllegalArgumentException("At least one seat required");
        if (selections.size() > MAX_SEATS)
            throw new IllegalArgumentException("Max " + MAX_SEATS + " seats per booking");

        var b = new Booking();
        b.id            = BookingId.generate();
        b.customerId    = customerId;
        b.showtimeId    = showtimeId;
        b.cinemaId      = cinemaId;
        b.movieId       = movieId;
        b.status        = BookingStatus.INITIATED;
        b.paymentMethod = paymentMethod;
        b.createdAt     = clock.instant();
        b.updatedAt     = clock.instant();
        b.expiresAt     = b.createdAt.plus(TTL);

        selections.forEach(s -> b.items.add(BookingItem.of(b, s.seatId(), s.seatCode(), s.category(), s.price())));
        b.subtotalValue  = b.items.stream().mapToLong(i -> i.getPrice().toLong()).sum();
        b.finalAmountValue = b.subtotalValue;
        b.registerEvent(new BookingInitiatedEvent(UUID.randomUUID(), b.id, customerId, showtimeId, b.createdAt));
        return b;
    }

    // ── Transitions ───────────────────────────────────────────────────────────
    public void markSeatsHeld(Instant at) { transition(BookingStatus.SEATS_HELD, at); }

    public void applyVoucher(VoucherCode code, Money discount, Instant at) {
        transition(BookingStatus.VOUCHER_APPLIED, at);
        this.appliedVoucher    = code;
        this.discountAmountValue = discount.toLong();
        this.finalAmountValue  = subtotalValue - discountAmountValue;
        if (finalAmountValue < 0) throw new IllegalArgumentException("Discount exceeds subtotal");
    }

    public void markPaymentPending(PaymentId paymentId, String paymentUrl, Instant at) {
        if (status != BookingStatus.SEATS_HELD && status != BookingStatus.VOUCHER_APPLIED)
            throw new InvalidBookingTransitionException(id, status, BookingStatus.PAYMENT_PENDING);
        this.status     = BookingStatus.PAYMENT_PENDING;
        this.paymentId  = paymentId;
        this.paymentUrl = paymentUrl;
        this.updatedAt  = at;
    }

    public void startConfirming(Instant at)            { transition(BookingStatus.CONFIRMING, at); }
    public void startCompensating(String reason, Instant at) {
        transition(BookingStatus.COMPENSATING, at);
        registerEvent(new BookingCompensationStartedEvent(UUID.randomUUID(), id, reason, at));
    }

    public void confirm(Instant at) {
        transition(BookingStatus.CONFIRMED, at);
        registerEvent(new BookingConfirmedEvent(UUID.randomUUID(), id, customerId, showtimeId,
            getSeatIds(), getFinalAmount(), appliedVoucher, paymentMethod, at));
    }

    public void cancel(String reason, Instant at) {
        if (!status.canTransitionTo(BookingStatus.CANCELLED) && status != BookingStatus.COMPENSATING)
            throw new InvalidBookingTransitionException(id, status, BookingStatus.CANCELLED);
        this.status    = BookingStatus.CANCELLED;
        this.updatedAt = at;
        registerEvent(new BookingCancelledEvent(UUID.randomUUID(), id, customerId, reason, at));
    }

    public void fail(String reason, Instant at) {
        this.status    = BookingStatus.FAILED;
        this.updatedAt = at;
        registerEvent(new BookingFailedEvent(UUID.randomUUID(), id, reason, at));
    }

    // ── Queries ───────────────────────────────────────────────────────────────
    public boolean isExpired(Clock clock) { return Instant.now(clock).isAfter(expiresAt); }
    public boolean hasVoucher()          { return appliedVoucher != null; }
    public boolean isPaid()              { return paymentId != null; }

    public List<SeatId> getSeatIds() {
        return items.stream().map(BookingItem::getSeatId).toList();
    }
    public Money getSubtotal()        { return Money.ofVnd(subtotalValue); }
    public Money getFinalAmount()     { return Money.ofVnd(finalAmountValue); }
    public Money getDiscountAmount()  { return Money.ofVnd(discountAmountValue); }

    @Override public BookingId getId() { return id; }

    private void transition(BookingStatus next, Instant at) {
        if (!status.canTransitionTo(next))
            throw new InvalidBookingTransitionException(id, status, next);
        this.status    = next;
        this.updatedAt = at;
    }
}
