package com.cinetix.showtime.infrastructure.persistence;

import com.cinetix.showtime.domain.model.SeatOccupancyStatus;
import com.cinetix.showtime.domain.model.SeatStatusEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeatStatusJpaRepository extends JpaRepository<SeatStatusEntry, UUID> {

    List<SeatStatusEntry> findByShowtimeId(UUID showtimeId);

    @Query("SELECT s FROM SeatStatusEntry s WHERE s.showtimeId = :sid AND s.seatId IN :seatIds")
    List<SeatStatusEntry> findByShowtimeIdAndSeatIdIn(@Param("sid") UUID showtimeId,
                                                        @Param("seatIds") List<UUID> seatIds);

    Optional<SeatStatusEntry> findByShowtimeIdAndSeatId(UUID showtimeId, UUID seatId);

    @Modifying
    @Query("UPDATE SeatStatusEntry s SET s.status = :status, s.bookingId = :bookingId, s.confirmedAt = :at " +
           "WHERE s.showtimeId = :sid AND s.seatId IN :seatIds")
    int confirmSeats(@Param("sid") UUID showtimeId, @Param("seatIds") List<UUID> seatIds,
                     @Param("bookingId") UUID bookingId, @Param("status") SeatOccupancyStatus status,
                     @Param("at") Instant at);

    @Query("SELECT COUNT(s) FROM SeatStatusEntry s WHERE s.showtimeId = :sid AND s.status = 'AVAILABLE'")
    int countAvailableSeats(@Param("sid") UUID showtimeId);
}
