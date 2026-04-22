package com.cinetix.booking.application.saga;

import com.cinetix.booking.application.dto.command.CreateBookingCommand;
import com.cinetix.booking.domain.model.Booking;
import com.cinetix.booking.domain.model.BookingStatus;
import com.cinetix.booking.domain.model.SagaStep;
import com.cinetix.booking.domain.model.valueobject.*;
import com.cinetix.booking.domain.port.outbound.*;
import com.cinetix.booking.infrastructure.persistence.SagaStateEntity;
import com.cinetix.booking.infrastructure.persistence.SagaStateJpaRepository;
import com.cinetix.common.exception.BusinessException;
import com.cinetix.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.Clock;
import java.util.UUID;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class BookingSagaOrchestrator {

    private final BookingRepository bookingRepository;
    private final SagaStateJpaRepository sagaStateRepository;
    private final ShowtimePort showtimePort;
    private final PaymentPort paymentPort;
    private final PromotionPort promotionPort;
    private final EventPublisherPort eventPublisher;
    private final Clock clock;

    /**
     * Starts the booking saga: hold seats → (optionally) validate voucher → initiate payment.
     * noRollbackFor ensures failure branches persist the FAILED booking before throwing.
     */
    @Transactional(noRollbackFor = BusinessException.class)
    public BookingId start(CreateBookingCommand cmd) {
        // Step 1: Create booking aggregate
        Booking booking = Booking.initiate(
                CustomerId.of(cmd.customerId()),
                ShowtimeId.of(cmd.showtimeId()),
                cmd.cinemaId(),
                cmd.movieId(),
                cmd.selections(),
                cmd.paymentMethod(),
                clock
        );

        // Step 2 & 3: Persist and publish initial events
        bookingRepository.save(booking);
        eventPublisher.publishAll(booking.pullDomainEvents());

        // Step 4: Create and persist saga state
        SagaStateEntity saga = SagaStateEntity.create(booking.getId().value());
        sagaStateRepository.save(saga);

        // Step 5: Hold seats
        ShowtimePort.HoldResult holdResult = showtimePort.holdSeats(
                booking.getShowtimeId(),
                booking.getSeatIds(),
                booking.getId(),
                600
        );

        if (holdResult instanceof ShowtimePort.HoldResult.Failure f) {
            booking.fail("Seats unavailable: " + f.errorCode(), clock.instant());
            bookingRepository.save(booking);
            eventPublisher.publishAll(booking.pullDomainEvents());
            updateSaga(booking.getId(), SagaStep.COMPENSATED, f.errorCode());
            throw new BusinessException("SEATS_UNAVAILABLE",
                    "Seats not available: " + String.join(",", f.unavailableSeats()));
        }

        // HoldResult.Success path
        booking.markSeatsHeld(clock.instant());
        updateSaga(booking.getId(), SagaStep.HOLD_SEATS, null);

        // Step 6: Validate voucher (optional)
        if (cmd.voucherCode() != null && !cmd.voucherCode().isBlank()) {
            PromotionPort.VoucherResult voucherResult = promotionPort.validateVoucher(
                    VoucherCode.of(cmd.voucherCode()),
                    booking.getCustomerId(),
                    booking.getFinalAmount(),
                    booking.getShowtimeId()
            );

            if (voucherResult instanceof PromotionPort.VoucherResult.Invalid inv) {
                tryReleaseSeats(booking, "Invalid voucher: " + inv.errorCode());
                booking.fail("Invalid voucher: " + inv.errorCode(), clock.instant());
                bookingRepository.save(booking);
                eventPublisher.publishAll(booking.pullDomainEvents());
                updateSaga(booking.getId(), SagaStep.COMPENSATED, inv.errorCode());
                throw new BusinessException(inv.errorCode(), inv.message());
            }

            // VoucherResult.Valid path
            PromotionPort.VoucherResult.Valid v = (PromotionPort.VoucherResult.Valid) voucherResult;
            booking.applyVoucher(VoucherCode.of(cmd.voucherCode()), v.discountAmount(), clock.instant());
            updateSaga(booking.getId(), SagaStep.VALIDATE_VOUCHER, null);
        }

        // Step 7: Initiate payment
        PaymentPort.PaymentResult paymentResult = paymentPort.initiatePayment(
                booking.getId(),
                booking.getCustomerId(),
                booking.getFinalAmount(),
                booking.getPaymentMethod()
        );

        if (paymentResult instanceof PaymentPort.PaymentResult.Failure f) {
            tryVoidVoucher(booking);
            tryReleaseSeats(booking, "Payment initiation failed: " + f.errorCode());
            booking.fail("Payment initiation failed: " + f.errorCode(), clock.instant());
            bookingRepository.save(booking);
            eventPublisher.publishAll(booking.pullDomainEvents());
            updateSaga(booking.getId(), SagaStep.COMPENSATED, f.errorCode());
            throw new BusinessException(f.errorCode(), f.message());
        }

        // PaymentResult.Success path
        PaymentPort.PaymentResult.Success s = (PaymentPort.PaymentResult.Success) paymentResult;
        booking.markPaymentPending(PaymentId.of(s.paymentId()), s.paymentUrl(), clock.instant());
        updateSaga(booking.getId(), SagaStep.AWAIT_PAYMENT, null);

        bookingRepository.save(booking);
        eventPublisher.publishAll(booking.pullDomainEvents());

        return booking.getId();
    }

    /**
     * Called when an asynchronous payment confirmation is received.
     */
    @Transactional
    public void onPaymentConfirmed(UUID bookingId) {
        Booking booking = bookingRepository.findById(BookingId.of(bookingId))
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        booking.startConfirming(clock.instant());
        bookingRepository.save(booking);

        try {
            showtimePort.confirmSeats(booking.getShowtimeId(), booking.getSeatIds(), booking.getId());
        } catch (Exception e) {
            log.error("Failed to confirm seats for booking {}: {}", bookingId, e.getMessage(), e);
            booking.startCompensating("Seat confirmation failed", clock.instant());
            tryRefundPayment(booking);
            booking.fail("Confirm seats failed", clock.instant());
            bookingRepository.save(booking);
            eventPublisher.publishAll(booking.pullDomainEvents());
            updateSaga(booking.getId(), SagaStep.COMPENSATED, "Seat confirmation failed");
            return;
        }

        booking.confirm(clock.instant());
        bookingRepository.save(booking);
        eventPublisher.publishAll(booking.pullDomainEvents());

        updateSaga(booking.getId(), SagaStep.COMPLETED, null);
    }

    /**
     * Called when an asynchronous payment failure is received.
     */
    @Transactional
    public void onPaymentFailed(UUID bookingId, String reason) {
        Booking booking = bookingRepository.findById(BookingId.of(bookingId))
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        BookingStatus status = booking.getStatus();
        if (status == BookingStatus.CONFIRMED
                || status == BookingStatus.CANCELLED
                || status == BookingStatus.FAILED) {
            log.warn("Booking {} is already in terminal status {}, ignoring payment failure", bookingId, status);
            return;
        }

        booking.startCompensating(reason, clock.instant());

        if (booking.hasVoucher()) {
            tryVoidVoucher(booking);
        }

        tryReleaseSeats(booking, reason);

        booking.cancel(reason, clock.instant());
        bookingRepository.save(booking);
        eventPublisher.publishAll(booking.pullDomainEvents());

        updateSaga(booking.getId(), SagaStep.COMPENSATED, reason);
    }

    /**
     * Called when the payment window has timed out.
     */
    @Transactional
    public void onPaymentTimeout(UUID bookingId) {
        onPaymentFailed(bookingId, "Payment timed out");
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private void tryReleaseSeats(Booking booking, String reason) {
        try {
            showtimePort.releaseSeats(booking.getShowtimeId(), booking.getSeatIds(), booking.getId(), reason);
        } catch (Exception e) {
            log.warn("Failed to release seats for booking {}: {}", booking.getId(), e.getMessage(), e);
        }
    }

    private void tryVoidVoucher(Booking booking) {
        try {
            promotionPort.voidVoucher(booking.getAppliedVoucher(), booking.getId());
        } catch (Exception e) {
            log.warn("Failed to void voucher for booking {}: {}", booking.getId(), e.getMessage(), e);
        }
    }

    private void tryRefundPayment(Booking booking) {
        try {
            paymentPort.refundPayment(booking.getPaymentId(), "Booking compensation");
        } catch (Exception e) {
            log.warn("Failed to refund payment for booking {}: {}", booking.getId(), e.getMessage(), e);
        }
    }

    private void updateSaga(BookingId id, SagaStep step, String reason) {
        sagaStateRepository.findById(id.value()).ifPresent(saga -> {
            saga.advance(step, reason);
            sagaStateRepository.save(saga);
        });
    }
}
