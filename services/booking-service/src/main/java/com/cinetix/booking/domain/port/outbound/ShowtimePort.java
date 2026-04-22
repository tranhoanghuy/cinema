package com.cinetix.booking.domain.port.outbound;

import com.cinetix.booking.domain.model.valueobject.*;
import com.cinetix.common.domain.valueobject.Money;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

public interface ShowtimePort {

    HoldResult holdSeats(ShowtimeId showtimeId, List<SeatId> seatIds,
                          BookingId bookingId, int ttlSeconds);

    void confirmSeats(ShowtimeId showtimeId, List<SeatId> seatIds, BookingId bookingId);

    void releaseSeats(ShowtimeId showtimeId, List<SeatId> seatIds,
                      BookingId bookingId, String reason);

    // Result types (sealed for exhaustive pattern matching)
    sealed interface HoldResult permits HoldResult.Success, HoldResult.Failure {
        record Success(Instant expiresAt, List<HeldSeat> heldSeats, int remainingSeats)
            implements HoldResult {}
        record Failure(String errorCode, List<String> unavailableSeats)
            implements HoldResult {}

        static HoldResult success(Instant expiresAt, List<HeldSeat> seats, int remaining) {
            return new Success(expiresAt, seats, remaining);
        }
        static HoldResult failure(String code, List<String> unavailable) {
            return new Failure(code, unavailable);
        }

        default void ifSuccessOrElse(Consumer<Success> onSuccess, Consumer<Failure> onFailure) {
            if (this instanceof Success s) onSuccess.accept(s);
            else if (this instanceof Failure f) onFailure.accept(f);
        }
    }

    record HeldSeat(String seatId, String seatCode, String category) {}
}
