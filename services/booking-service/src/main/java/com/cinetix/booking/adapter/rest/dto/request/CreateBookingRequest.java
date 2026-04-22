package com.cinetix.booking.adapter.rest.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public record CreateBookingRequest(
    @NotNull UUID showtimeId,
    @NotNull UUID cinemaId,
    @NotNull UUID movieId,
    @NotEmpty @Size(min = 1, max = 8) List<SeatRequest> seats,
    @NotBlank String paymentMethod,
    String voucherCode
) {
    public record SeatRequest(
        @NotNull UUID seatId,
        @NotBlank String seatCode,
        @NotBlank String category,
        @Min(0) long price
    ) {}
}
