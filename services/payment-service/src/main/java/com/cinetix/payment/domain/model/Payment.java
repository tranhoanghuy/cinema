package com.cinetix.payment.domain.model;

import com.cinetix.common.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends AggregateRoot<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "amount", nullable = false)
    private long amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "VND";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 30)
    private PaymentMethod method;

    @Column(name = "psp_provider", length = 50)
    private String pspProvider;

    @Column(name = "payment_url", length = 1000)
    private String paymentUrl;

    @Column(name = "psp_transaction_id", length = 255)
    private String pspTransactionId;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public static Payment initiate(UUID bookingId, UUID customerId, long amount, String currency,
                                    PaymentMethod method, String pspProvider,
                                    String paymentUrl, String idempotencyKey, int ttlMinutes) {
        var p = new Payment();
        p.bookingId      = bookingId;
        p.customerId     = customerId;
        p.amount         = amount;
        p.currency       = currency;
        p.status         = PaymentStatus.PENDING;
        p.method         = method;
        p.pspProvider    = pspProvider;
        p.paymentUrl     = paymentUrl;
        p.idempotencyKey = idempotencyKey;
        p.expiresAt      = Instant.now().plusSeconds(ttlMinutes * 60L);
        p.createdAt      = Instant.now();
        p.updatedAt      = Instant.now();
        return p;
    }

    public void complete(String pspTransactionId) {
        if (status != PaymentStatus.PENDING)
            throw new IllegalStateException("Cannot complete payment in status: " + status);
        this.status           = PaymentStatus.COMPLETED;
        this.pspTransactionId = pspTransactionId;
        this.completedAt      = Instant.now();
        this.updatedAt        = Instant.now();
    }

    public void fail(String reason) {
        this.status        = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt     = Instant.now();
    }

    public void cancel(String reason) {
        if (status == PaymentStatus.COMPLETED)
            throw new IllegalStateException("Cannot cancel completed payment");
        this.status        = PaymentStatus.CANCELLED;
        this.failureReason = reason;
        this.updatedAt     = Instant.now();
    }

    @Override public UUID getId() { return id; }
}
