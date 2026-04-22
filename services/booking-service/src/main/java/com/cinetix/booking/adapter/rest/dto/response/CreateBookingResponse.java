package com.cinetix.booking.adapter.rest.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CreateBookingResponse(
    UUID bookingId,
    String status,
    String paymentUrl,
    long finalAmount,
    String currency,
    Instant expiresAt
) {}
