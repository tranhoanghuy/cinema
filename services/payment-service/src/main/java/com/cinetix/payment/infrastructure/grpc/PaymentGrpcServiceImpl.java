package com.cinetix.payment.infrastructure.grpc;

import com.cinetix.grpc.common.v1.Error;
import com.cinetix.grpc.payment.v1.*;
import com.cinetix.payment.application.PaymentApplicationService;
import com.cinetix.payment.domain.model.Payment;
import com.cinetix.payment.domain.model.Refund;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Instant;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class PaymentGrpcServiceImpl extends PaymentServiceGrpc.PaymentServiceImplBase {

    private final PaymentApplicationService appService;

    @Override
    public void initiatePayment(InitiatePaymentRequest request,
                                 StreamObserver<InitiatePaymentResponse> observer) {
        try {
            UUID bookingId  = UUID.fromString(request.getBookingId());
            UUID customerId = UUID.fromString(request.getCustomerId());
            long amount     = request.getAmount().getAmountMinorUnits();
            String currency = request.getAmount().getCurrency();

            Payment payment = appService.initiatePayment(
                bookingId, customerId, amount, currency,
                request.getMethod(), request.getIdempotencyKey(),
                request.getReturnUrl(), request.getDescription()
            );

            observer.onNext(InitiatePaymentResponse.newBuilder()
                .setSuccess(InitiatePaymentSuccess.newBuilder()
                    .setPaymentId(payment.getId().toString())
                    .setPaymentUrl(payment.getPaymentUrl() != null ? payment.getPaymentUrl() : "")
                    .setPspProvider(payment.getPspProvider() != null ? payment.getPspProvider() : "")
                    .setExpiresAt(toProtoTimestamp(payment.getExpiresAt()))
                    .build())
                .build());
            observer.onCompleted();

        } catch (Exception e) {
            log.error("initiatePayment gRPC error: {}", e.getMessage(), e);
            observer.onNext(InitiatePaymentResponse.newBuilder()
                .setError(Error.newBuilder().setCode("PAYMENT_INIT_FAILED").setMessage(e.getMessage()).build())
                .build());
            observer.onCompleted();
        }
    }

    @Override
    public void refundPayment(RefundPaymentRequest request,
                               StreamObserver<RefundPaymentResponse> observer) {
        try {
            UUID paymentId = UUID.fromString(request.getPaymentId());
            UUID bookingId = UUID.fromString(request.getBookingId());
            long amount    = request.getRefundAmount().getAmountMinorUnits();

            Refund refund = appService.initiateRefund(paymentId, bookingId, amount,
                request.getReason(), request.getIdempotencyKey());

            observer.onNext(RefundPaymentResponse.newBuilder()
                .setSuccess(RefundPaymentSuccess.newBuilder()
                    .setRefundId(refund.getId().toString())
                    .setStatus(refund.getStatus().name())
                    .setRefundAmount(com.cinetix.grpc.common.v1.Money.newBuilder()
                        .setAmountMinorUnits(refund.getAmount())
                        .setCurrency(refund.getCurrency()).build())
                    .build())
                .build());
            observer.onCompleted();

        } catch (Exception e) {
            log.error("refundPayment gRPC error: {}", e.getMessage(), e);
            observer.onNext(RefundPaymentResponse.newBuilder()
                .setError(Error.newBuilder().setCode("REFUND_FAILED").setMessage(e.getMessage()).build())
                .build());
            observer.onCompleted();
        }
    }

    @Override
    public void getPaymentStatus(GetPaymentStatusRequest request,
                                  StreamObserver<GetPaymentStatusResponse> observer) {
        try {
            UUID paymentId = UUID.fromString(request.getPaymentId());
            Payment payment = appService.getById(paymentId);

            var builder = GetPaymentStatusResponse.newBuilder()
                .setPaymentId(payment.getId().toString())
                .setBookingId(payment.getBookingId().toString())
                .setStatus(payment.getStatus().name())
                .setAmount(com.cinetix.grpc.common.v1.Money.newBuilder()
                    .setAmountMinorUnits(payment.getAmount())
                    .setCurrency(payment.getCurrency()).build())
                .setMethod(payment.getMethod().name());
            if (payment.getFailureReason() != null) builder.setFailureReason(payment.getFailureReason());
            if (payment.getCompletedAt() != null) builder.setCompletedAt(toProtoTimestamp(payment.getCompletedAt()));

            observer.onNext(builder.build());
            observer.onCompleted();

        } catch (Exception e) {
            log.error("getPaymentStatus gRPC error: {}", e.getMessage(), e);
            observer.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void cancelPayment(CancelPaymentRequest request,
                               StreamObserver<CancelPaymentResponse> observer) {
        try {
            UUID paymentId = UUID.fromString(request.getPaymentId());
            appService.cancelPayment(paymentId, request.getReason());
            observer.onNext(CancelPaymentResponse.newBuilder().setSuccess(true).setStatus("CANCELLED").build());
            observer.onCompleted();
        } catch (Exception e) {
            log.error("cancelPayment gRPC error: {}", e.getMessage(), e);
            observer.onNext(CancelPaymentResponse.newBuilder()
                .setSuccess(false)
                .setError(Error.newBuilder().setCode("CANCEL_FAILED").setMessage(e.getMessage()).build())
                .build());
            observer.onCompleted();
        }
    }

    private Timestamp toProtoTimestamp(Instant instant) {
        return instant == null ? Timestamp.getDefaultInstance()
            : Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
    }
}
