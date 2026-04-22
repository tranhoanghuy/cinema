package com.cinetix.booking.application.dto.query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BookingDetailDto(
    UUID bookingId,
    UUID customerId,
    String status,
    String movieTitle,
    String cinemaName,
    String screenName,
    Instant showtimeStart,
    List<SeatDto> seats,
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
    public record SeatDto(UUID seatId, String seatCode, String category, long price) {}
}
