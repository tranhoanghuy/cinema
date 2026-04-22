package com.cinetix.booking.domain.model.valueobject;

import com.cinetix.common.domain.valueobject.Money;

import java.util.UUID;

public record SeatSelection(
    SeatId seatId,
    String seatCode,
    String category,
    Money  price
) {
    public static SeatSelection of(String seatId, String seatCode, String category, long priceVnd) {
        return new SeatSelection(
            SeatId.of(UUID.fromString(seatId)),
            seatCode,
            category,
            Money.ofVnd(priceVnd)
        );
    }
}
