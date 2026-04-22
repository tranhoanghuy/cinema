package com.cinetix.showtime.infrastructure.persistence;

import com.cinetix.showtime.domain.model.Showtime;
import com.cinetix.showtime.domain.model.ShowtimeStatus;
import com.cinetix.showtime.domain.model.valueobject.ShowtimeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ShowtimeJpaRepository extends JpaRepository<Showtime, ShowtimeId> {

    @Query("SELECT s FROM Showtime s WHERE s.movieId = :movieId AND s.status = :status ORDER BY s.startTime ASC")
    List<Showtime> findByMovieIdAndStatus(@Param("movieId") UUID movieId,
                                           @Param("status") ShowtimeStatus status);

    @Query("SELECT s FROM Showtime s WHERE s.cinemaId = :cinemaId AND s.startTime >= :from AND s.startTime <= :to ORDER BY s.startTime ASC")
    List<Showtime> findByCinemaIdAndDateRange(@Param("cinemaId") UUID cinemaId,
                                               @Param("from") Instant from,
                                               @Param("to") Instant to);

    @Query("SELECT s FROM Showtime s WHERE s.status = 'ON_SALE' AND s.startTime >= :from ORDER BY s.startTime ASC")
    List<Showtime> findOnSaleFrom(@Param("from") Instant from);
}
