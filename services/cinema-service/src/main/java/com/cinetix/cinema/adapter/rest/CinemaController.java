package com.cinetix.cinema.adapter.rest;

import com.cinetix.cinema.domain.model.*;
import com.cinetix.cinema.infrastructure.persistence.*;
import com.cinetix.common.exception.ResourceNotFoundException;
import com.cinetix.common.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cinemas")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaJpaRepository  cinemaRepo;
    private final ScreenJpaRepository  screenRepo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Cinema>>> list(@RequestParam(required = false) String city) {
        List<Cinema> list = city != null ? cinemaRepo.findByCityAndActiveTrue(city)
                                        : cinemaRepo.findByActiveTrue();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Cinema>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(cinemaRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cinema not found: " + id))));
    }

    @GetMapping("/{id}/screens")
    public ResponseEntity<ApiResponse<List<Screen>>> listScreens(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(screenRepo.findActiveByCinemaId(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Cinema>> create(@RequestBody Cinema cinema) {
        return ResponseEntity.status(201).body(ApiResponse.success(cinemaRepo.save(cinema)));
    }

    @PostMapping("/{id}/screens")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Screen>> addScreen(@PathVariable UUID id,
                                                          @RequestBody Screen screen) {
        Cinema cinema = cinemaRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cinema not found: " + id));
        screen.setCinema(cinema);
        return ResponseEntity.status(201).body(ApiResponse.success(screenRepo.save(screen)));
    }
}
