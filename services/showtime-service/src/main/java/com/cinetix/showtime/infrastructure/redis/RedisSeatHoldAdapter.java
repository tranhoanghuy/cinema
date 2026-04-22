package com.cinetix.showtime.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Manages seat holds using Redis SET NX with TTL.
 * Key: cinetix:seat:{showtimeId}:{seatId} → bookingId
 * Reverse index: cinetix:booking:{bookingId}:seats → Set<"showtimeId:seatId">
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisSeatHoldAdapter {

    private final StringRedisTemplate redis;

    private static final String SEAT_KEY_PREFIX    = "cinetix:seat:";
    private static final String BOOKING_KEY_PREFIX = "cinetix:booking:";

    public record HoldResult(boolean success, List<String> unavailableSeats, Instant expiresAt) {
        public static HoldResult success(Instant at)         { return new HoldResult(true, List.of(), at); }
        public static HoldResult failure(List<String> seats) { return new HoldResult(false, seats, null); }
    }

    public HoldResult holdSeats(UUID showtimeId, List<UUID> seatIds, UUID bookingId, int ttlSeconds) {
        List<String> unavailable = new ArrayList<>();

        // Phase 1: check current hold status
        for (UUID seatId : seatIds) {
            String key = seatKey(showtimeId, seatId);
            String holder = redis.opsForValue().get(key);
            if (holder != null && !holder.equals(bookingId.toString())) {
                unavailable.add(seatId.toString());
            }
        }
        if (!unavailable.isEmpty()) {
            return HoldResult.failure(unavailable);
        }

        // Phase 2: attempt SET NX on each seat
        List<UUID> held = new ArrayList<>();
        Duration ttl = Duration.ofSeconds(ttlSeconds);
        for (UUID seatId : seatIds) {
            String key = seatKey(showtimeId, seatId);
            Boolean set = redis.opsForValue().setIfAbsent(key, bookingId.toString(), ttl);
            if (Boolean.FALSE.equals(set)) {
                // Race condition - another booking grabbed this seat; release all we held
                held.forEach(h -> redis.delete(seatKey(showtimeId, h)));
                unavailable.add(seatId.toString());
                log.warn("Seat race condition: showtimeId={} seatId={} bookingId={}", showtimeId, seatId, bookingId);
                return HoldResult.failure(unavailable);
            }
            held.add(seatId);
        }

        // Phase 3: store reverse index for ForceRelease
        String bookingSeatsKey = bookingSeatsKey(bookingId, showtimeId);
        seatIds.forEach(s -> redis.opsForSet().add(bookingSeatsKey, s.toString()));
        redis.expire(bookingSeatsKey, ttl.plusSeconds(60));

        Instant expiresAt = Instant.now().plusSeconds(ttlSeconds);
        log.info("Held {} seats for bookingId={} showtimeId={} ttl={}s", seatIds.size(), bookingId, showtimeId, ttlSeconds);
        return HoldResult.success(expiresAt);
    }

    public int releaseSeats(UUID showtimeId, List<UUID> seatIds, UUID bookingId) {
        int count = 0;
        for (UUID seatId : seatIds) {
            String key = seatKey(showtimeId, seatId);
            String holder = redis.opsForValue().get(key);
            if (bookingId.toString().equals(holder)) {
                redis.delete(key);
                count++;
            }
        }
        redis.delete(bookingSeatsKey(bookingId, showtimeId));
        log.info("Released {} seats for bookingId={} showtimeId={}", count, bookingId, showtimeId);
        return count;
    }

    public List<UUID> forceReleaseByBooking(UUID bookingId, UUID showtimeId) {
        String bookingKey = bookingSeatsKey(bookingId, showtimeId);
        Set<String> members = redis.opsForSet().members(bookingKey);
        List<UUID> released = new ArrayList<>();
        if (members != null) {
            for (String seatIdStr : members) {
                UUID seatId = UUID.fromString(seatIdStr);
                redis.delete(seatKey(showtimeId, seatId));
                released.add(seatId);
            }
            redis.delete(bookingKey);
        }
        log.info("Force released {} seats for bookingId={}", released.size(), bookingId);
        return released;
    }

    public boolean isSeatHeld(UUID showtimeId, UUID seatId) {
        return redis.hasKey(seatKey(showtimeId, seatId));
    }

    public String getHolder(UUID showtimeId, UUID seatId) {
        return redis.opsForValue().get(seatKey(showtimeId, seatId));
    }

    private String seatKey(UUID showtimeId, UUID seatId) {
        return SEAT_KEY_PREFIX + showtimeId + ":" + seatId;
    }

    private String bookingSeatsKey(UUID bookingId, UUID showtimeId) {
        return BOOKING_KEY_PREFIX + bookingId + ":seats:" + showtimeId;
    }
}
