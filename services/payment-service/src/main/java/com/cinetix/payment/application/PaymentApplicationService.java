package com.cinetix.payment.application;

import com.cinetix.payment.domain.event.*;
import com.cinetix.payment.domain.model.*;
import com.cinetix.payment.infrastructure.messaging.OutboxEventPublisherAdapter;
import com.cinetix.payment.infrastructure.persistence.PaymentJpaRepository;
import com.cinetix.payment.infrastructure.persistence.RefundJpaRepository;
import com.cinetix.common.exception.BusinessException;
import com.cinetix.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentApplicationService {

    private final PaymentJpaRepository      paymentRepo;
    private final RefundJpaRepository       refundRepo;
    private final OutboxEventPublisherAdapter publisher;

    /** Initiate a payment. Returns the new Payment or the existing one if idempotency key matches. */
    public Payment initiatePayment(UUID bookingId, UUID customerId, long amount, String currency,
                                    String methodStr, String idempotencyKey,
                                    String returnUrl, String description) {
        // Idempotency check
        var existing = paymentRepo.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Idempotent payment request: key={} paymentId={}", idempotencyKey, existing.get().getId());
            return existing.get();
        }

        PaymentMethod method;
        try { method = PaymentMethod.valueOf(methodStr.toUpperCase()); }
        catch (Exception e) { method = PaymentMethod.VNPAY; }

        // Generate a simulated payment URL (in production, call PSP API here)
        String pspProvider = method.name();
        String paymentUrl  = buildSimulatedPaymentUrl(bookingId, amount, returnUrl);

        Payment payment = Payment.initiate(bookingId, customerId, amount, currency,
            method, pspProvider, paymentUrl, idempotencyKey, 15);
        paymentRepo.save(payment);

        publisher.publishAll(List.of(new PaymentInitiatedEvent(
            UUID.randomUUID(), payment.getId(), bookingId,
            customerId, amount, currency, method.name(), paymentUrl, Instant.now()
        )));

        log.info("Payment initiated: paymentId={} bookingId={} amount={}", payment.getId(), bookingId, amount);
        return payment;
    }

    /** Callback from PSP: confirm payment completed. */
    public Payment confirmPayment(UUID paymentId, String pspTransactionId) {
        Payment payment = paymentRepo.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
        payment.complete(pspTransactionId);
        paymentRepo.save(payment);

        publisher.publishAll(List.of(new PaymentCompletedEvent(
            UUID.randomUUID(), payment.getId(), payment.getBookingId(),
            payment.getCustomerId(), payment.getAmount(), payment.getCurrency(),
            payment.getMethod().name(), Instant.now()
        )));

        log.info("Payment completed: paymentId={} bookingId={}", paymentId, payment.getBookingId());
        return payment;
    }

    /** Callback from PSP: payment failed. */
    public Payment failPayment(UUID paymentId, String reason) {
        Payment payment = paymentRepo.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
        payment.fail(reason);
        paymentRepo.save(payment);

        publisher.publishAll(List.of(new PaymentFailedEvent(
            UUID.randomUUID(), payment.getId(), payment.getBookingId(), reason, Instant.now()
        )));
        return payment;
    }

    /** Initiate refund (called by Saga compensation). */
    public Refund initiateRefund(UUID paymentId, UUID bookingId, long refundAmount,
                                  String reason, String idempotencyKey) {
        var existing = refundRepo.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) return existing.get();

        Payment payment = paymentRepo.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
        if (payment.getStatus() != PaymentStatus.COMPLETED)
            throw new BusinessException("PAYMENT_NOT_COMPLETED", "Cannot refund non-completed payment");

        Refund refund = Refund.builder()
            .paymentId(paymentId)
            .bookingId(bookingId)
            .amount(refundAmount)
            .reason(reason)
            .idempotencyKey(idempotencyKey)
            .build();
        refundRepo.save(refund);

        // Simulate immediate refund completion (in production, call PSP refund API)
        refund.complete("SIMULATED-REFUND-" + UUID.randomUUID());
        refundRepo.save(refund);

        publisher.publishAll(List.of(new PaymentFailedEvent(
            UUID.randomUUID(), paymentId, bookingId, "Refunded: " + reason, Instant.now()
        )));
        return refund;
    }

    public Payment cancelPayment(UUID paymentId, String reason) {
        Payment payment = paymentRepo.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
        payment.cancel(reason);
        paymentRepo.save(payment);

        publisher.publishAll(List.of(new PaymentFailedEvent(
            UUID.randomUUID(), payment.getId(), payment.getBookingId(), reason, Instant.now()
        )));
        return payment;
    }

    @Transactional(readOnly = true)
    public Payment getByBookingId(UUID bookingId) {
        return paymentRepo.findByBookingId(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found for booking: " + bookingId));
    }

    @Transactional(readOnly = true)
    public Payment getById(UUID paymentId) {
        return paymentRepo.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
    }

    private String buildSimulatedPaymentUrl(UUID bookingId, long amount, String returnUrl) {
        return "https://sandbox.vnpay.vn/paymentv2/vpcpay.html?bookingId=" + bookingId
            + "&amount=" + amount + "&returnUrl=" + returnUrl;
    }
}
