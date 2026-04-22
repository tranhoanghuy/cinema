package com.cinetix.booking.application.dto.command;

import com.cinetix.booking.domain.model.valueobject.SeatSelection;

import java.util.List;
import java.util.UUID;

public record CreateBookingCommand(
    UUID customerId,
    UUID showtimeId,
    UUID cinemaId,
    UUID movieId,
    List<SeatSelection> selections,
    String paymentMethod,
    String voucherCode
) {}
