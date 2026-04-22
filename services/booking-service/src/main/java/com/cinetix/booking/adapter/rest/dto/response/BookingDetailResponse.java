package com.cinetix.booking.adapter.rest.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BookingDetailResponse(
    UUID bookingId,
    String status,
    String movieTitle,
    String cinemaName,
    String screenName,
    Instant showtimeStart,
    List<SeatDetail> seats,
    long subtotal,
    long discountAmount,
    long finalAmount,
    String currency,
    String voucherCode,
    String paymentMethod,
    String paymentUrl,
    List<String> qrCodes,
    Instant createdAt,
    Instant expiresAt
) {
    public record SeatDetail(UUID seatId, String seatCode, String category, long price) {}
}
