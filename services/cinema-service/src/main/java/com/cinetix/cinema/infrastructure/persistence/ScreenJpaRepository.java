package com.cinetix.cinema.infrastructure.persistence;

import com.cinetix.cinema.domain.model.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ScreenJpaRepository extends JpaRepository<Screen, UUID> {

    @Query("SELECT s FROM Screen s WHERE s.cinema.id = :cinemaId AND s.active = true")
    List<Screen> findActiveByCinemaId(@Param("cinemaId") UUID cinemaId);
}
