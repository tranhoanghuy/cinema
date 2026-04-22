package com.cinetix.movie.adapter.rest;

import com.cinetix.common.exception.ResourceNotFoundException;
import com.cinetix.common.web.ApiResponse;
import com.cinetix.movie.domain.model.Movie;
import com.cinetix.movie.domain.model.MovieStatus;
import com.cinetix.movie.infrastructure.persistence.MovieJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieJpaRepository movieRepo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Movie>>> listNowShowing(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        Page<Movie> result = movieRepo.findByStatusOrderByReleaseDateDesc(
            MovieStatus.NOW_SHOWING, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(result.getContent()));
    }

    @GetMapping("/coming-soon")
    public ResponseEntity<ApiResponse<List<Movie>>> listComingSoon() {
        return ResponseEntity.ok(ApiResponse.success(movieRepo.findByStatus(MovieStatus.COMING_SOON)));
    }

    @GetMapping("/{id}")
    @Cacheable(value = "movies", key = "#id")
    public ResponseEntity<ApiResponse<Movie>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(movieRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + id))));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Movie>>> search(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.success(movieRepo.searchByTitle(q)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Movie>> create(@RequestBody Movie movie) {
        return ResponseEntity.status(201).body(ApiResponse.success(movieRepo.save(movie)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "movies", key = "#id")
    public ResponseEntity<ApiResponse<Movie>> update(@PathVariable UUID id, @RequestBody Movie movie) {
        if (!movieRepo.existsById(id)) throw new ResourceNotFoundException("Movie not found: " + id);
        movie.setId(id);
        return ResponseEntity.ok(ApiResponse.success(movieRepo.save(movie)));
    }
}
