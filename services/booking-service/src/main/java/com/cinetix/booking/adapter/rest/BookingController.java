package com.cinetix.booking.adapter.rest;

import com.cinetix.booking.adapter.rest.dto.request.CreateBookingRequest;
import com.cinetix.booking.adapter.rest.dto.response.BookingDetailResponse;
import com.cinetix.booking.adapter.rest.dto.response.CreateBookingResponse;
import com.cinetix.booking.adapter.rest.mapper.BookingRestMapper;
import com.cinetix.booking.domain.model.valueobject.BookingId;
import com.cinetix.booking.domain.port.inbound.*;
import com.cinetix.common.web.ApiResponse;
import com.cinetix.common.web.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final CreateBookingUseCase createBookingUseCase;
    private final CancelBookingUseCase cancelBookingUseCase;
    private final GetBookingUseCase    getBookingUseCase;
    private final BookingRestMapper    mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<CreateBookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        var command   = mapper.toCommand(request, extractUserId(jwt));
        var bookingId = createBookingUseCase.execute(command);
        var detail    = getBookingUseCase.findById(bookingId);
        return ApiResponse.success(mapper.toCreateResponse(detail));
    }

    @GetMapping("/{bookingId}")
    public ApiResponse<BookingDetailResponse> getBooking(
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal Jwt jwt) {

        var detail = getBookingUseCase.findById(BookingId.of(bookingId));
        if (!detail.customerId().equals(extractUserId(jwt))) {
            throw new com.cinetix.common.exception.BusinessException(
                "FORBIDDEN", "Access denied");
        }
        return ApiResponse.success(mapper.toDetailResponse(detail));
    }

    @GetMapping("/my")
    public ApiResponse<PagedResponse<BookingDetailResponse>> listMyBookings(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String status,
            @AuthenticationPrincipal Jwt jwt) {

        var paged = getBookingUseCase.findByCustomer(extractUserId(jwt), status, page, size);
        return ApiResponse.success(
            PagedResponse.of(paged.content().stream().map(mapper::toDetailResponse).toList(),
                paged.page(), paged.size(), paged.totalElements())
        );
    }

    @DeleteMapping("/{bookingId}")
    public ApiResponse<Void> cancelBooking(
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal Jwt jwt) {
        cancelBookingUseCase.execute(bookingId, extractUserId(jwt));
        return ApiResponse.success(null);
    }

    private UUID extractUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
