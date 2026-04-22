package com.cinetix.booking.domain.model;

import java.util.Map;
import java.util.Set;

public enum BookingStatus {
    INITIATED, SEATS_HELD, VOUCHER_APPLIED, PAYMENT_PENDING,
    CONFIRMING, CONFIRMED, COMPENSATING, CANCELLED, FAILED;

    private static final Map<BookingStatus, Set<BookingStatus>> TRANSITIONS = Map.of(
        INITIATED,       Set.of(SEATS_HELD, FAILED, CANCELLED),
        SEATS_HELD,      Set.of(VOUCHER_APPLIED, PAYMENT_PENDING, COMPENSATING, FAILED),
        VOUCHER_APPLIED, Set.of(PAYMENT_PENDING, COMPENSATING, FAILED),
        PAYMENT_PENDING, Set.of(CONFIRMING, COMPENSATING),
        CONFIRMING,      Set.of(CONFIRMED, COMPENSATING),
        COMPENSATING,    Set.of(CANCELLED),
        CONFIRMED,       Set.of(),
        CANCELLED,       Set.of(),
        FAILED,          Set.of()
    );

    public boolean canTransitionTo(BookingStatus next) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(next);
    }

    public boolean isTerminal() {
        return this == CONFIRMED || this == CANCELLED || this == FAILED;
    }
}
