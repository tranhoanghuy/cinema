package com.cinetix.booking.infrastructure.grpc;

import com.cinetix.booking.domain.model.valueobject.BookingId;
import com.cinetix.booking.domain.model.valueobject.CustomerId;
import com.cinetix.booking.domain.model.valueobject.ShowtimeId;
import com.cinetix.booking.domain.model.valueobject.VoucherCode;
import com.cinetix.booking.domain.port.outbound.PromotionPort;
import com.cinetix.common.domain.valueobject.Money;
import com.cinetix.grpc.promotion.v1.PromotionServiceGrpc;
import com.cinetix.grpc.promotion.v1.RedeemVoucherRequest;
import com.cinetix.grpc.promotion.v1.ValidateVoucherRequest;
import com.cinetix.grpc.promotion.v1.ValidateVoucherResponse;
import com.cinetix.grpc.promotion.v1.ValidateVoucherSuccess;
import com.cinetix.grpc.promotion.v1.VoidVoucherRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PromotionGrpcAdapter implements PromotionPort {

    @GrpcClient("promotion-service")
    private PromotionServiceGrpc.PromotionServiceBlockingStub stub;

    private static final long DEADLINE_MS = 3000L;

    @Override
    @CircuitBreaker(name = "promotion-service", fallbackMethod = "validateVoucherFallback")
    public VoucherResult validateVoucher(VoucherCode code, CustomerId customerId,
                                         Money orderAmount, ShowtimeId showtimeId) {
        com.cinetix.grpc.common.v1.Money protoMoney = com.cinetix.grpc.common.v1.Money.newBuilder()
                .setAmountMinorUnits(orderAmount.toLong())
                .setCurrency("VND")
                .build();

        ValidateVoucherRequest request = ValidateVoucherRequest.newBuilder()
                .setCode(code.toString())
                .setCustomerId(customerId.toString())
                .setOrderAmount(protoMoney)
                .setShowtimeId(showtimeId.toString())
                .build();

        ValidateVoucherResponse response = stub
                .withDeadlineAfter(DEADLINE_MS, TimeUnit.MILLISECONDS)
                .validateVoucher(request);

        if (response.hasSuccess()) {
            ValidateVoucherSuccess s = response.getSuccess();
            return VoucherResult.valid(
                    s.getVoucherId(),
                    s.getPromotionName(),
                    Money.ofVnd(s.getDiscountAmount().getAmountMinorUnits()),
                    Money.ofVnd(s.getFinalAmount().getAmountMinorUnits()));
        } else {
            return VoucherResult.invalid(
                    response.getError().getCode(),
                    response.getError().getMessage());
        }
    }

    public PromotionPort.VoucherResult validateVoucherFallback(VoucherCode code,
                                                                CustomerId customerId,
                                                                Money orderAmount,
                                                                ShowtimeId showtimeId,
                                                                Throwable ex) {
        log.warn("validateVoucher circuit breaker fallback: {}", ex.getMessage());
        return PromotionPort.VoucherResult.invalid("PROMOTION_SERVICE_UNAVAILABLE",
                "Promotion service unavailable");
    }

    @Override
    public void redeemVoucher(VoucherCode code, BookingId bookingId, CustomerId customerId) {
        try {
            RedeemVoucherRequest request = RedeemVoucherRequest.newBuilder()
                    .setCode(code.toString())
                    .setCustomerId(customerId.toString())
                    .setBookingId(bookingId.toString())
                    .setIdempotencyKey(bookingId + "-redeem-" + code)
                    .build();

            stub.withDeadlineAfter(DEADLINE_MS, TimeUnit.MILLISECONDS)
                    .redeemVoucher(request);
        } catch (Exception e) {
            log.error("redeemVoucher failed for bookingId={} code={}: {}",
                    bookingId, code, e.getMessage(), e);
        }
    }

    @Override
    public void voidVoucher(VoucherCode code, BookingId bookingId) {
        try {
            VoidVoucherRequest request = VoidVoucherRequest.newBuilder()
                    .setVoucherId(code.toString())
                    .setBookingId(bookingId.toString())
                    .setReason("Booking cancelled")
                    .build();

            stub.withDeadlineAfter(DEADLINE_MS, TimeUnit.MILLISECONDS)
                    .voidVoucher(request);
        } catch (Exception e) {
            log.error("voidVoucher failed for bookingId={} code={}: {}",
                    bookingId, code, e.getMessage(), e);
        }
    }
}
