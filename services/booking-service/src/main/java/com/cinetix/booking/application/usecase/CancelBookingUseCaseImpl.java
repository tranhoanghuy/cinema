package com.cinetix.booking.application.usecase;

import com.cinetix.booking.application.saga.BookingSagaOrchestrator;
import com.cinetix.booking.domain.model.Booking;
import com.cinetix.booking.domain.model.BookingStatus;
import com.cinetix.booking.domain.model.valueobject.BookingId;
import com.cinetix.booking.domain.port.inbound.CancelBookingUseCase;
import com.cinetix.booking.domain.port.outbound.BookingRepository;
import com.cinetix.booking.domain.port.outbound.EventPublisherPort;
import com.cinetix.booking.domain.port.outbound.PromotionPort;
import com.cinetix.booking.domain.port.outbound.ShowtimePort;
import com.cinetix.common.exception.BusinessException;
import com.cinetix.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CancelBookingUseCaseImpl implements CancelBookingUseCase {

    private final BookingRepository bookingRepository;
    private final BookingSagaOrchestrator saga;
    private final Clock clock;
    private final EventPublisherPort eventPublisher;
    private final ShowtimePort showtimePort;
    private final PromotionPort promotionPort;

    @Override
    public void execute(UUID bookingId, UUID requesterId) {
        Booking booking = bookingRepository.findById(BookingId.of(bookingId))
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        // Verify the requester is the booking owner
        if (!booking.getCustomerId().value().equals(requesterId)) {
            throw new BusinessException("FORBIDDEN", "Cannot cancel another user's booking");
        }

        // Reject if already in a terminal state
        if (booking.getStatus().isTerminal()) {
            throw new BusinessException("BOOKING_ALREADY_TERMINAL",
                    "Cannot cancel a booking in status: " + booking.getStatus());
        }

        BookingStatus status = booking.getStatus();

        // If payment is in-flight, delegate to the saga which handles refund + seat release
        if (status == BookingStatus.PAYMENT_PENDING || status == BookingStatus.CONFIRMING) {
            saga.onPaymentFailed(bookingId, "Cancelled by customer");
            return;
        }

        // INITIATED has no external resources held — cancel directly without compensation
        if (status == BookingStatus.INITIATED) {
            booking.cancel("Cancelled by customer", clock.instant());
            bookingRepository.save(booking);
            eventPublisher.publishAll(booking.pullDomainEvents());
            return;
        }

        // For SEATS_HELD / VOUCHER_APPLIED — compensate then cancel
        booking.startCompensating("Cancelled by customer", clock.instant());

        if (booking.hasVoucher()) {
            try {
                promotionPort.voidVoucher(booking.getAppliedVoucher(), booking.getId());
            } catch (Exception e) {
                log.warn("Failed to void voucher during customer cancellation of booking {}: {}",
                        bookingId, e.getMessage(), e);
            }
        }

        try {
            showtimePort.releaseSeats(booking.getShowtimeId(), booking.getSeatIds(),
                    booking.getId(), "Cancelled by customer");
        } catch (Exception e) {
            log.warn("Failed to release seats during customer cancellation of booking {}: {}",
                    bookingId, e.getMessage(), e);
        }

        booking.cancel("Cancelled by customer", clock.instant());
        bookingRepository.save(booking);
        eventPublisher.publishAll(booking.pullDomainEvents());
    }
}
