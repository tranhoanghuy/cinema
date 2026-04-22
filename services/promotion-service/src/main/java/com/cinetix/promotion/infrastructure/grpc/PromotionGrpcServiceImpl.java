package com.cinetix.promotion.infrastructure.grpc;

import com.cinetix.grpc.common.v1.Error;
import com.cinetix.grpc.promotion.v1.*;
import com.cinetix.promotion.application.PromotionApplicationService;
import com.cinetix.promotion.domain.model.Promotion;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class PromotionGrpcServiceImpl extends PromotionServiceGrpc.PromotionServiceImplBase {

    private final PromotionApplicationService appService;

    @Override
    public void validateVoucher(ValidateVoucherRequest request,
                                 StreamObserver<ValidateVoucherResponse> observer) {
        try {
            UUID customerId = UUID.fromString(request.getCustomerId());
            UUID cinemaId   = request.getCinemaId().isBlank() ? null : UUID.fromString(request.getCinemaId());
            UUID movieId    = request.getMovieId().isBlank()  ? null : UUID.fromString(request.getMovieId());
            long orderAmount = request.getOrderAmount().getAmountMinorUnits();

            PromotionApplicationService.ValidationResult result =
                appService.validateVoucher(request.getCode(), customerId, orderAmount, cinemaId, movieId);

            observer.onNext(ValidateVoucherResponse.newBuilder()
                .setSuccess(ValidateVoucherSuccess.newBuilder()
                    .setVoucherId(result.voucherId().toString())
                    .setPromotionId(result.promotionId().toString())
                    .setPromotionName(result.promotionName())
                    .setDiscountType(result.discountType().name())
                    .setDiscountPercent(result.discountPercent())
                    .setDiscountAmount(com.cinetix.grpc.common.v1.Money.newBuilder()
                        .setAmountMinorUnits(result.discountAmount()).setCurrency("VND").build())
                    .setFinalAmount(com.cinetix.grpc.common.v1.Money.newBuilder()
                        .setAmountMinorUnits(result.finalAmount()).setCurrency("VND").build())
                    .setValidUntil(toProto(result.validUntil()))
                    .build())
                .build());
            observer.onCompleted();

        } catch (com.cinetix.common.exception.BusinessException e) {
            observer.onNext(ValidateVoucherResponse.newBuilder()
                .setError(Error.newBuilder().setCode(e.getCode()).setMessage(e.getMessage()).build())
                .build());
            observer.onCompleted();
        } catch (Exception e) {
            log.error("validateVoucher gRPC error: {}", e.getMessage(), e);
            observer.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void redeemVoucher(RedeemVoucherRequest request,
                               StreamObserver<RedeemVoucherResponse> observer) {
        try {
            UUID voucherId  = UUID.fromString(request.getVoucherId());
            UUID customerId = UUID.fromString(request.getCustomerId());
            UUID bookingId  = UUID.fromString(request.getBookingId());

            UUID redemptionId = appService.redeemVoucher(
                request.getCode(), voucherId, customerId, bookingId,
                request.getDiscountApplied().getAmountMinorUnits(),
                request.getIdempotencyKey()
            );

            observer.onNext(RedeemVoucherResponse.newBuilder()
                .setSuccess(RedeemVoucherSuccess.newBuilder()
                    .setRedemptionId(redemptionId.toString())
                    .setRedeemedAt(toProto(Instant.now()))
                    .build())
                .build());
            observer.onCompleted();

        } catch (com.cinetix.common.exception.BusinessException e) {
            observer.onNext(RedeemVoucherResponse.newBuilder()
                .setError(Error.newBuilder().setCode(e.getCode()).setMessage(e.getMessage()).build())
                .build());
            observer.onCompleted();
        } catch (Exception e) {
            log.error("redeemVoucher gRPC error: {}", e.getMessage(), e);
            observer.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void voidVoucher(VoidVoucherRequest request, StreamObserver<VoidVoucherResponse> observer) {
        try {
            UUID voucherId = UUID.fromString(request.getVoucherId());
            UUID bookingId = UUID.fromString(request.getBookingId());
            appService.voidVoucher(voucherId, bookingId, request.getIdempotencyKey());
            observer.onNext(VoidVoucherResponse.newBuilder().setSuccess(true).setStatus("ACTIVE").build());
            observer.onCompleted();
        } catch (Exception e) {
            log.error("voidVoucher gRPC error: {}", e.getMessage(), e);
            observer.onNext(VoidVoucherResponse.newBuilder()
                .setSuccess(false)
                .setError(Error.newBuilder().setCode("VOID_FAILED").setMessage(e.getMessage()).build())
                .build());
            observer.onCompleted();
        }
    }

    @Override
    public void listActivePromotions(ListActivePromotionsRequest request,
                                      StreamObserver<ListActivePromotionsResponse> observer) {
        try {
            UUID cinemaId = request.getCinemaId().isBlank() ? null : UUID.fromString(request.getCinemaId());
            UUID movieId  = request.getMovieId().isBlank()  ? null : UUID.fromString(request.getMovieId());

            List<Promotion> promos = appService.listActivePromotions(cinemaId, movieId);
            var builder = ListActivePromotionsResponse.newBuilder();
            promos.forEach(p -> builder.addPromotions(PromotionSummary.newBuilder()
                .setId(p.getId().toString())
                .setName(p.getName())
                .setDescription(p.getDescription() != null ? p.getDescription() : "")
                .setDiscountType(p.getDiscountType().name())
                .setDiscountPercent(p.getDiscountPercent())
                .setEndsAt(toProto(p.getEndsAt()))
                .build()));
            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            log.error("listActivePromotions gRPC error: {}", e.getMessage(), e);
            observer.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private Timestamp toProto(Instant instant) {
        return Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
    }
}
