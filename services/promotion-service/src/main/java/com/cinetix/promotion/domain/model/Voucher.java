package com.cinetix.promotion.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vouchers",
    uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "promotion_id", nullable = false)
    private UUID promotionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private VoucherStatus status = VoucherStatus.ACTIVE;

    @Column(name = "assigned_customer_id")
    private UUID assignedCustomerId;

    @Column(name = "booking_id")
    private UUID bookingId;

    @Column(name = "redemption_id")
    private UUID redemptionId;

    @Column(name = "redeemed_at")
    private Instant redeemedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    public boolean isUsable(UUID customerId, Instant now) {
        if (status != VoucherStatus.ACTIVE) return false;
        if (now.isAfter(expiresAt)) return false;
        if (assignedCustomerId != null && !assignedCustomerId.equals(customerId)) return false;
        return true;
    }

    public void redeem(UUID bookingId, UUID redemptionId) {
        this.status = VoucherStatus.REDEEMED;
        this.bookingId = bookingId;
        this.redemptionId = redemptionId;
        this.redeemedAt = Instant.now();
    }

    public void voidVoucher() {
        if (status != VoucherStatus.REDEEMED)
            throw new IllegalStateException("Cannot void non-redeemed voucher");
        this.status = VoucherStatus.ACTIVE;
        this.bookingId = null;
        this.redemptionId = null;
        this.redeemedAt = null;
    }
}
