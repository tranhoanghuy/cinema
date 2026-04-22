package com.cinetix.promotion.infrastructure.persistence;

import com.cinetix.promotion.domain.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PromotionJpaRepository extends JpaRepository<Promotion, UUID> {

    @Query("SELECT p FROM Promotion p WHERE p.active = true AND p.startsAt <= :now AND p.endsAt > :now " +
           "AND (p.cinemaId IS NULL OR p.cinemaId = :cinemaId) " +
           "AND (p.movieId IS NULL OR p.movieId = :movieId)")
    List<Promotion> findActivePromotions(@Param("now") Instant now,
                                          @Param("cinemaId") UUID cinemaId,
                                          @Param("movieId") UUID movieId);
}
