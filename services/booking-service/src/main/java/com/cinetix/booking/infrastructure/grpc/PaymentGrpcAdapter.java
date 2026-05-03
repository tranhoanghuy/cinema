package com.cinetix.booking.infrastructure.grpc;

import com.cinetix.booking.domain.model.valueobject.BookingId;
import com.cinetix.booking.domain.model.valueobject.CustomerId;
import com.cinetix.booking.domain.model.valueobject.PaymentId;
import com.cinetix.booking.domain.port.outbound.PaymentPort;
import com.cinetix.common.domain.valueobject.Money;
import com.cinetix.grpc.payment.v1.InitiatePaymentRequest;
import com.cinetix.grpc.payment.v1.InitiatePaymentResponse;
import com.cinetix.grpc.payment.v1.InitiatePaymentSuccess;
import com.cinetix.grpc.payment.v1.PaymentServiceGrpc;
import com.cinetix.grpc.payment.v1.RefundPaymentRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PaymentGrpcAdapter implements PaymentPort {

    @GrpcClient("payment-service")
    private PaymentServiceGrpc.PaymentServiceBlockingStub stub;

    private static final long DEADLINE_MS = 5000L;

    @Override
    @CircuitBreaker(name = "payment-service", fallbackMethod = "initiatePaymentFallback")
    public PaymentResult initiatePayment(BookingId bookingId, CustomerId customerId,
                                         Money amount, String method) {
        com.cinetix.grpc.common.v1.Money protoMoney = com.cinetix.grpc.common.v1.Money.newBuilder()
                .setAmountMinorUnits(amount.toLong())
                .setCurrency("VND")
                .build();

        InitiatePaymentRequest request = InitiatePaymentRequest.newBuilder()
                .setBookingId(bookingId.toString())
                .setCustomerId(customerId.toString())
                .setAmount(protoMoney)
                .setMethod(method)
                .setIdempotencyKey(bookingId + "-pay")
                .build();

        InitiatePaymentResponse response = stub
                .withDeadlineAfter(DEADLINE_MS, TimeUnit.MILLISECONDS)
                .initiatePayment(request);

        if (response.hasSuccess()) {
            InitiatePaymentSuccess success = response.getSuccess();
            return PaymentResult.success(
                    success.getPaymentId(),
                    success.getPaymentUrl(),
                    success.getPspProvider());
        } else {
            return PaymentResult.failure(
                    response.getError().getCode(),
                    response.getError().getMessage());
        }
    }

    public PaymentPort.PaymentResult initiatePaymentFallback(BookingId bookingId,
                                                              CustomerId customerId,
                                                              Money amount, String method,
                                                              Throwable ex) {
        log.error("initiatePayment circuit breaker fallback: {}", ex.getMessage());
        return PaymentPort.PaymentResult.failure("PAYMENT_SERVICE_UNAVAILABLE",
                "Payment service unavailable");
    }

    @Override
    public void refundPayment(PaymentId paymentId, String reason) {
        try {
            RefundPaymentRequest request = RefundPaymentRequest.newBuilder()
                    .setPaymentId(paymentId.toString())
                    .setReason(reason)
                    .setIdempotencyKey(paymentId + "-refund")
                    .build();

            stub.withDeadlineAfter(DEADLINE_MS, TimeUnit.MILLISECONDS)
                    .refundPayment(request);
        } catch (Exception e) {
            log.error("refundPayment failed for paymentId={}: {}", paymentId, e.getMessage(), e);
        }
    }
}
