package com.cinetix.payment.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refunds")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "amount", nullable = false)
    private long amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "VND";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RefundStatus status = RefundStatus.PENDING;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;

    @Column(name = "psp_refund_id", length = 255)
    private String pspRefundId;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    public void complete(String pspRefundId) {
        this.status = RefundStatus.COMPLETED;
        this.pspRefundId = pspRefundId;
        this.completedAt = Instant.now();
    }
}
