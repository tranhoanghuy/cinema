package com.cinetix.promotion.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "discount_percent")
    private int discountPercent;

    @Column(name = "discount_amount")
    private long discountAmount;

    @Column(name = "max_discount")
    private long maxDiscount;

    @Column(name = "min_order_amount", nullable = false)
    @Builder.Default
    private long minOrderAmount = 0L;

    @Column(name = "cinema_id")
    private UUID cinemaId;

    @Column(name = "movie_id")
    private UUID movieId;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(name = "max_uses")
    @Builder.Default
    private int maxUses = Integer.MAX_VALUE;

    @Column(name = "current_uses", nullable = false)
    @Builder.Default
    private int currentUses = 0;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    public boolean isValid(Instant now) {
        return active && now.isAfter(startsAt) && now.isBefore(endsAt) && currentUses < maxUses;
    }

    public long computeDiscount(long orderAmount) {
        if (orderAmount < minOrderAmount) return 0L;
        long discount = switch (discountType) {
            case PERCENT -> Math.min(orderAmount * discountPercent / 100L,
                maxDiscount > 0 ? maxDiscount : Long.MAX_VALUE);
            case FIXED   -> Math.min(discountAmount, orderAmount);
        };
        return discount;
    }
}
