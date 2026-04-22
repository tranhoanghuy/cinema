package com.cinetix.booking.infrastructure.grpc;

import com.cinetix.booking.domain.model.valueobject.BookingId;
import com.cinetix.booking.domain.model.valueobject.SeatId;
import com.cinetix.booking.domain.model.valueobject.ShowtimeId;
import com.cinetix.booking.domain.port.outbound.ShowtimePort;
import com.cinetix.common.exception.BusinessException;
import com.cinetix.grpc.showtime.v1.ConfirmSeatsRequest;
import com.cinetix.grpc.showtime.v1.ConfirmSeatsResponse;
import com.cinetix.grpc.showtime.v1.HoldSeatsRequest;
import com.cinetix.grpc.showtime.v1.HoldSeatsResponse;
import com.cinetix.grpc.showtime.v1.HoldSeatsSuccess;
import com.cinetix.grpc.showtime.v1.HoldSeatsFailure;
import com.cinetix.grpc.showtime.v1.ReleaseSeatsRequest;
import com.cinetix.grpc.showtime.v1.SeatHoldDetail;
import com.cinetix.grpc.showtime.v1.ShowtimeServiceGrpc;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ShowtimeGrpcAdapter implements ShowtimePort {

    @GrpcClient("showtime-service")
    private ShowtimeServiceGrpc.ShowtimeServiceBlockingStub stub;

    private static final long DEADLINE_MS = 5000L;

    @Override
    @CircuitBreaker(name = "showtime-service", fallbackMethod = "holdSeatsFallback")
    @Retry(name = "showtime-hold")
    public HoldResult holdSeats(ShowtimeId showtimeId, List<SeatId> seatIds,
                                BookingId bookingId, int ttlSeconds) {
        HoldSeatsRequest request = HoldSeatsRequest.newBuilder()
                .setShowtimeId(showtimeId.toString())
                .addAllSeatIds(seatIds.stream().map(SeatId::toString).toList())
                .setBookingId(bookingId.toString())
                .setTtlSeconds(ttlSeconds)
                .setIdempotencyKey(bookingId + "-hold")
                .build();

        HoldSeatsResponse response = stub
                .withDeadlineAfter(DEADLINE_MS, TimeUnit.MILLISECONDS)
                .holdSeats(request);

        if (response.hasSuccess()) {
            HoldSeatsSuccess success = response.getSuccess();
            Instant expiresAt = Instant.ofEpochSecond(
                    success.getExpiresAt().getSeconds(),
                    success.getExpiresAt().getNanos());
            List<ShowtimePort.HeldSeat> heldSeats = success.getHeldSeatsList().stream()
                    .map(s -> new ShowtimePort.HeldSeat(s.getSeatId(), s.getSeatCode(), s.getCategory()))
                    .toList();
            return HoldResult.success(expiresAt, heldSeats, success.getRemainingSeats());
        } else {
            HoldSeatsFailure failure = response.getFailure();
            return HoldResult.failure(failure.getError().getCode(),
                    failure.getUnavailableSeatsList());
        }
    }

    public ShowtimePort.HoldResult holdSeatsFallback(ShowtimeId showtimeId, List<SeatId> seatIds,
                                                     BookingId bookingId, int ttlSeconds,
                                                     Throwable ex) {
        log.error("holdSeats circuit breaker fallback: {}", ex.getMessage());
        return ShowtimePort.HoldResult.failure("SHOWTIME_SERVICE_UNAVAILABLE", List.of());
    }

    @Override
    public void confirmSeats(ShowtimeId showtimeId, List<SeatId> seatIds, BookingId bookingId) {
        ConfirmSeatsRequest request = ConfirmSeatsRequest.newBuilder()
                .setShowtimeId(showtimeId.toString())
                .addAllSeatIds(seatIds.stream().map(SeatId::toString).toList())
                .setBookingId(bookingId.toString())
                .build();

        ConfirmSeatsResponse response = stub
                .withDeadlineAfter(DEADLINE_MS, TimeUnit.MILLISECONDS)
                .confirmSeats(request);

        if (response.hasError()) {
            throw new BusinessException("CONFIRM_SEATS_FAILED", response.getError().getMessage());
        }
    }

    @Override
    public void releaseSeats(ShowtimeId showtimeId, List<SeatId> seatIds,
                             BookingId bookingId, String reason) {
        try {
            ReleaseSeatsRequest request = ReleaseSeatsRequest.newBuilder()
                    .setShowtimeId(showtimeId.toString())
                    .addAllSeatIds(seatIds.stream().map(SeatId::toString).toList())
                    .setBookingId(bookingId.toString())
                    .setReason(reason)
                    .build();

            stub.withDeadlineAfter(DEADLINE_MS, TimeUnit.MILLISECONDS)
                    .releaseSeats(request);
        } catch (Exception e) {
            log.warn("releaseSeats best-effort failed for bookingId={}: {}", bookingId, e.getMessage());
        }
    }
}
