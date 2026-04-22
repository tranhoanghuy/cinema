package com.cinetix.showtime.infrastructure.grpc;

import com.cinetix.grpc.common.v1.Error;
import com.cinetix.grpc.showtime.v1.*;
import com.cinetix.showtime.application.ShowtimeApplicationService;
import com.cinetix.showtime.domain.model.SeatCategory;
import com.cinetix.showtime.domain.model.SeatStatusEntry;
import com.cinetix.showtime.domain.model.Showtime;
import com.cinetix.showtime.infrastructure.redis.RedisSeatHoldAdapter;
import com.cinetix.showtime.infrastructure.persistence.SeatStatusJpaRepository;
import com.cinetix.showtime.infrastructure.persistence.ShowtimeJpaRepository;
import com.cinetix.showtime.infrastructure.websocket.SeatStatusBroadcaster;
import com.cinetix.showtime.domain.model.valueobject.ShowtimeId;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class ShowtimeGrpcServiceImpl extends ShowtimeServiceGrpc.ShowtimeServiceImplBase {

    private final ShowtimeJpaRepository   showtimeRepo;
    private final SeatStatusJpaRepository seatStatusRepo;
    private final RedisSeatHoldAdapter    seatHoldAdapter;
    private final SeatStatusBroadcaster   broadcaster;
    private final ShowtimeApplicationService appService;

    @Override
    public void getShowtime(GetShowtimeRequest request, StreamObserver<GetShowtimeResponse> observer) {
        try {
            ShowtimeId id = ShowtimeId.of(request.getShowtimeId());
            Showtime showtime = showtimeRepo.findById(id).orElse(null);
            if (showtime == null) {
                observer.onNext(GetShowtimeResponse.newBuilder()
                    .setError(Error.newBuilder().setCode("NOT_FOUND")
                        .setMessage("Showtime not found: " + id).build())
                    .build());
                observer.onCompleted();
                return;
            }

            ShowtimeDetail detail = ShowtimeDetail.newBuilder()
                .setShowtimeId(showtime.getId().toString())
                .setMovieId(showtime.getMovieId().toString())
                .setMovieTitle(showtime.getMovieTitle())
                .setCinemaId(showtime.getCinemaId().toString())
                .setCinemaName(showtime.getCinemaName())
                .setScreenId(showtime.getScreenId().toString())
                .setScreenName(showtime.getScreenName())
                .setFormat(showtime.getFormat())
                .setAudioType(showtime.getAudioType())
                .setStartTime(toProtoTimestamp(showtime.getStartTime()))
                .setEndTime(toProtoTimestamp(showtime.getEndTime()))
                .setStatus(showtime.getStatus().name())
                .setTotalSeats(showtime.getTotalSeats())
                .setAvailableSeats(showtime.getAvailableSeats())
                .setBasePrice(com.cinetix.grpc.common.v1.Money.newBuilder()
                    .setAmountMinorUnits(showtime.getBasePrice()).setCurrency(showtime.getCurrency()).build())
                .setVipPrice(com.cinetix.grpc.common.v1.Money.newBuilder()
                    .setAmountMinorUnits(showtime.getVipPrice()).setCurrency(showtime.getCurrency()).build())
                .setCouplePrice(com.cinetix.grpc.common.v1.Money.newBuilder()
                    .setAmountMinorUnits(showtime.getCouplePrice()).setCurrency(showtime.getCurrency()).build())
                .build();

            observer.onNext(GetShowtimeResponse.newBuilder().setShowtime(detail).build());
            observer.onCompleted();
        } catch (Exception e) {
            log.error("getShowtime error: {}", e.getMessage(), e);
            observer.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    @Transactional
    public void holdSeats(HoldSeatsRequest request, StreamObserver<HoldSeatsResponse> observer) {
        try {
            UUID showtimeId = UUID.fromString(request.getShowtimeId());
            UUID bookingId  = UUID.fromString(request.getBookingId());
            List<UUID> seatIds = request.getSeatIdsList().stream()
                .map(UUID::fromString).collect(Collectors.toList());
            int ttl = request.getTtlSeconds() > 0 ? request.getTtlSeconds() : 600;

            // Validate showtime
            Showtime showtime = showtimeRepo.findById(ShowtimeId.of(showtimeId)).orElse(null);
            if (showtime == null) {
                observer.onNext(HoldSeatsResponse.newBuilder()
                    .setFailure(HoldSeatsFailure.newBuilder()
                        .setError(Error.newBuilder().setCode("SHOWTIME_NOT_FOUND").setMessage("Showtime not found").build())
                        .build())
                    .build());
                observer.onCompleted();
                return;
            }
            if (!showtime.getStatus().isSellable()) {
                observer.onNext(HoldSeatsResponse.newBuilder()
                    .setFailure(HoldSeatsFailure.newBuilder()
                        .setError(Error.newBuilder().setCode("SHOWTIME_NOT_ON_SALE")
                            .setMessage("Showtime is not on sale: " + showtime.getStatus()).build())
                        .build())
                    .build());
                observer.onCompleted();
                return;
            }

            // Validate seats exist and are available in DB
            List<SeatStatusEntry> seats = seatStatusRepo.findByShowtimeIdAndSeatIdIn(showtimeId, seatIds);
            if (seats.size() != seatIds.size()) {
                observer.onNext(HoldSeatsResponse.newBuilder()
                    .setFailure(HoldSeatsFailure.newBuilder()
                        .setError(Error.newBuilder().setCode("SEATS_NOT_FOUND").setMessage("Some seats not found").build())
                        .build())
                    .build());
                observer.onCompleted();
                return;
            }

            List<String> bookedSeats = seats.stream()
                .filter(s -> !s.isAvailableInDb())
                .map(s -> s.getSeatId().toString())
                .collect(Collectors.toList());
            if (!bookedSeats.isEmpty()) {
                observer.onNext(HoldSeatsResponse.newBuilder()
                    .setFailure(HoldSeatsFailure.newBuilder()
                        .setError(Error.newBuilder().setCode("SEATS_ALREADY_BOOKED").setMessage("Seats are already booked").build())
                        .addAllUnavailableSeats(bookedSeats)
                        .build())
                    .build());
                observer.onCompleted();
                return;
            }

            // Attempt Redis hold
            RedisSeatHoldAdapter.HoldResult result = seatHoldAdapter.holdSeats(showtimeId, seatIds, bookingId, ttl);
            if (!result.success()) {
                observer.onNext(HoldSeatsResponse.newBuilder()
                    .setFailure(HoldSeatsFailure.newBuilder()
                        .setError(Error.newBuilder().setCode("SEATS_HELD_BY_ANOTHER").setMessage("Seats held by another booking").build())
                        .addAllUnavailableSeats(result.unavailableSeats())
                        .build())
                    .build());
                observer.onCompleted();
                return;
            }

            // Build held seat details
            List<String> seatIdStrings = seatIds.stream().map(UUID::toString).toList();
            broadcaster.broadcastSeatUpdate(showtimeId, seatIdStrings, "HELD");

            List<SeatHoldDetail> heldDetails = seats.stream().map(seat ->
                SeatHoldDetail.newBuilder()
                    .setSeatId(seat.getSeatId().toString())
                    .setSeatCode(seat.getSeatCode())
                    .setCategory(seat.getCategory().name())
                    .setPrice(com.cinetix.grpc.common.v1.Money.newBuilder()
                        .setAmountMinorUnits(seat.getUnitPrice())
                        .setCurrency("VND").build())
                    .build()
            ).collect(Collectors.toList());

            observer.onNext(HoldSeatsResponse.newBuilder()
                .setSuccess(HoldSeatsSuccess.newBuilder()
                    .setExpiresAt(toProtoTimestamp(result.expiresAt()))
                    .addAllHeldSeats(heldDetails)
                    .setRemainingSeats(showtime.getAvailableSeats() - seatIds.size())
                    .build())
                .build());
            observer.onCompleted();
            log.info("Seats held: showtimeId={} bookingId={} count={}", showtimeId, bookingId, seatIds.size());

        } catch (Exception e) {
            log.error("holdSeats error: {}", e.getMessage(), e);
            observer.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    @Transactional
    public void releaseSeats(ReleaseSeatsRequest request, StreamObserver<ReleaseSeatsResponse> observer) {
        try {
            UUID showtimeId = UUID.fromString(request.getShowtimeId());
            UUID bookingId  = UUID.fromString(request.getBookingId());
            List<UUID> seatIds = request.getSeatIdsList().stream().map(UUID::fromString).toList();

            int released = seatHoldAdapter.releaseSeats(showtimeId, seatIds, bookingId);
            List<String> seatIdStrings = seatIds.stream().map(UUID::toString).toList();
            broadcaster.broadcastSeatUpdate(showtimeId, seatIdStrings, "AVAILABLE");

            observer.onNext(ReleaseSeatsResponse.newBuilder()
                .setSuccess(true)
                .setReleasedCount(released)
                .build());
            observer.onCompleted();
        } catch (Exception e) {
            log.error("releaseSeats error: {}", e.getMessage(), e);
            observer.onNext(ReleaseSeatsResponse.newBuilder()
                .setSuccess(false)
                .setError(Error.newBuilder().setCode("RELEASE_FAILED").setMessage(e.getMessage()).build())
                .build());
            observer.onCompleted();
        }
    }

    @Override
    @Transactional
    public void confirmSeats(ConfirmSeatsRequest request, StreamObserver<ConfirmSeatsResponse> observer) {
        try {
            UUID showtimeId = UUID.fromString(request.getShowtimeId());
            UUID bookingId  = UUID.fromString(request.getBookingId());
            List<UUID> seatIds = request.getSeatIdsList().stream().map(UUID::fromString).toList();

            // Remove Redis holds
            seatHoldAdapter.releaseSeats(showtimeId, seatIds, bookingId);

            // Update DB to BOOKED
            Instant now = Instant.now();
            seatStatusRepo.confirmSeats(showtimeId, seatIds, bookingId,
                com.cinetix.showtime.domain.model.SeatOccupancyStatus.BOOKED, now);

            // Decrement available seats on showtime
            showtimeRepo.findById(ShowtimeId.of(showtimeId)).ifPresent(showtime -> {
                showtime.decrementAvailableSeats(seatIds.size());
                showtimeRepo.save(showtime);
            });

            List<String> seatIdStrings = seatIds.stream().map(UUID::toString).toList();
            broadcaster.broadcastSeatUpdate(showtimeId, seatIdStrings, "BOOKED");

            observer.onNext(ConfirmSeatsResponse.newBuilder()
                .setSuccess(ConfirmSeatsSuccess.newBuilder()
                    .setConfirmedAt(toProtoTimestamp(now))
                    .addAllConfirmedSeatIds(seatIdStrings)
                    .build())
                .build());
            observer.onCompleted();
            log.info("Seats confirmed: showtimeId={} bookingId={} count={}", showtimeId, bookingId, seatIds.size());

        } catch (Exception e) {
            log.error("confirmSeats error: {}", e.getMessage(), e);
            observer.onNext(ConfirmSeatsResponse.newBuilder()
                .setError(Error.newBuilder().setCode("CONFIRM_FAILED").setMessage(e.getMessage()).build())
                .build());
            observer.onCompleted();
        }
    }

    @Override
    @Transactional
    public void forceReleaseByBooking(ForceReleaseByBookingRequest request,
                                       StreamObserver<ReleaseSeatsResponse> observer) {
        try {
            UUID bookingId  = UUID.fromString(request.getBookingId());
            UUID showtimeId = UUID.fromString(request.getShowtimeId());

            List<UUID> released = seatHoldAdapter.forceReleaseByBooking(bookingId, showtimeId);
            List<String> seatIdStrings = released.stream().map(UUID::toString).toList();
            if (!released.isEmpty()) {
                broadcaster.broadcastSeatUpdate(showtimeId, seatIdStrings, "AVAILABLE");
            }

            observer.onNext(ReleaseSeatsResponse.newBuilder()
                .setSuccess(true)
                .setReleasedCount(released.size())
                .build());
            observer.onCompleted();
        } catch (Exception e) {
            log.error("forceReleaseByBooking error: {}", e.getMessage(), e);
            observer.onNext(ReleaseSeatsResponse.newBuilder()
                .setSuccess(false)
                .setError(Error.newBuilder().setCode("FORCE_RELEASE_FAILED").setMessage(e.getMessage()).build())
                .build());
            observer.onCompleted();
        }
    }

    private Timestamp toProtoTimestamp(Instant instant) {
        return Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();
    }
}
