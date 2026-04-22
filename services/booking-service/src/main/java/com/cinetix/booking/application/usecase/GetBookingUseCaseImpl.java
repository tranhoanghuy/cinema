package com.cinetix.booking.application.usecase;

import com.cinetix.booking.application.dto.query.BookingDetailDto;
import com.cinetix.booking.domain.model.Booking;
import com.cinetix.booking.domain.model.valueobject.BookingId;
import com.cinetix.booking.domain.model.valueobject.CustomerId;
import com.cinetix.booking.domain.port.inbound.GetBookingUseCase;
import com.cinetix.booking.domain.port.outbound.BookingRepository;
import com.cinetix.common.exception.ResourceNotFoundException;
import com.cinetix.common.web.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetBookingUseCaseImpl implements GetBookingUseCase {

    private final BookingRepository bookingRepository;

    @Override
    public BookingDetailDto findById(BookingId id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
        return toDto(booking);
    }

    @Override
    public PagedResponse<BookingDetailDto> findByCustomer(UUID customerId, String status, int page, int size) {
        CustomerId cid = CustomerId.of(customerId);
        List<Booking> bookings = bookingRepository.findByCustomerId(cid, status, page, size);
        long total = bookingRepository.countByCustomerId(cid, status);
        return PagedResponse.of(bookings.stream().map(this::toDto).toList(), page, size, total);
    }

    private BookingDetailDto toDto(Booking b) {
        List<BookingDetailDto.SeatDto> seats = b.getItems().stream()
                .map(i -> new BookingDetailDto.SeatDto(
                        i.getSeatId().value(),
                        i.getSeatCode(),
                        i.getSeatCategory(),
                        i.getPrice().toLong()
                ))
                .toList();

        return new BookingDetailDto(
                b.getId().value(),
                b.getCustomerId().value(),
                b.getStatus().name(),
                null,   // movieTitle — not available in booking write model
                null,   // cinemaName — not available in booking write model
                null,   // screenName — not available in booking write model
                null,   // showtimeStart — not available in booking write model
                seats,
                b.getSubtotal().toLong(),
                b.getDiscountAmount().toLong(),
                b.getFinalAmount().toLong(),
                b.getCurrency(),
                b.getAppliedVoucher() != null ? b.getAppliedVoucher().value() : null,
                b.getPaymentMethod(),
                b.getPaymentUrl(),
                List.of(),  // qrCodes — generated separately
                b.getCreatedAt(),
                b.getExpiresAt()
        );
    }
}
