package com.cinetix.cinema.infrastructure.persistence;

import com.cinetix.cinema.domain.model.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CinemaJpaRepository extends JpaRepository<Cinema, UUID> {
    List<Cinema> findByActiveTrue();
    List<Cinema> findByCityAndActiveTrue(String city);
}
