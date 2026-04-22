package com.cinetix.booking.adapter.rest.mapper;

import com.cinetix.booking.adapter.rest.dto.request.CreateBookingRequest;
import com.cinetix.booking.adapter.rest.dto.response.*;
import com.cinetix.booking.application.dto.command.CreateBookingCommand;
import com.cinetix.booking.application.dto.query.BookingDetailDto;
import com.cinetix.booking.domain.model.valueobject.SeatSelection;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BookingRestMapper {

    public CreateBookingCommand toCommand(CreateBookingRequest req, UUID userId) {
        var selections = req.seats().stream()
            .map(s -> SeatSelection.of(s.seatId().toString(), s.seatCode(),
                                       s.category(), s.price()))
            .toList();
        return new CreateBookingCommand(userId, req.showtimeId(), req.cinemaId(),
            req.movieId(), selections, req.paymentMethod(), req.voucherCode());
    }

    public CreateBookingResponse toCreateResponse(BookingDetailDto dto) {
        return new CreateBookingResponse(dto.bookingId(), dto.status(),
            dto.paymentUrl(), dto.finalAmount(), dto.currency(), dto.expiresAt());
    }

    public BookingDetailResponse toDetailResponse(BookingDetailDto dto) {
        var seats = dto.seats() == null ? java.util.List.of() :
            dto.seats().stream()
                .map(s -> new BookingDetailResponse.SeatDetail(s.seatId(), s.seatCode(),
                                                               s.category(), s.price()))
                .toList();
        return new BookingDetailResponse(dto.bookingId(), dto.status(), dto.movieTitle(),
            dto.cinemaName(), dto.screenName(), dto.showtimeStart(), seats,
            dto.subtotal(), dto.discountAmount(), dto.finalAmount(), dto.currency(),
            dto.voucherCode(), dto.paymentMethod(), dto.paymentUrl(),
            dto.qrCodes(), dto.createdAt(), dto.expiresAt());
    }
}
