package com.cinetix.movie.infrastructure.persistence;

import com.cinetix.movie.domain.model.Movie;
import com.cinetix.movie.domain.model.MovieStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MovieJpaRepository extends JpaRepository<Movie, UUID> {

    List<Movie> findByStatus(MovieStatus status);

    Page<Movie> findByStatus(MovieStatus status, Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE m.status = :status ORDER BY m.releaseDate DESC")
    Page<Movie> findByStatusOrderByReleaseDateDesc(@Param("status") MovieStatus status, Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(m.originalTitle) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Movie> searchByTitle(@Param("q") String query);
}
