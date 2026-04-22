package com.cinetix.showtime.adapter.rest;

import com.cinetix.common.web.ApiResponse;
import com.cinetix.showtime.application.ShowtimeApplicationService;
import com.cinetix.showtime.domain.model.SeatCategory;
import com.cinetix.showtime.domain.model.Showtime;
import com.cinetix.showtime.domain.model.SeatStatusEntry;
import com.cinetix.showtime.infrastructure.persistence.ShowtimeJpaRepository;
import com.cinetix.showtime.domain.model.valueobject.ShowtimeId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeApplicationService appService;
    private final ShowtimeJpaRepository      showtimeRepo;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShowtimeDto>> getById(@PathVariable UUID id) {
        Showtime s = appService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(toDto(s)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShowtimeDto>>> listOnSale() {
        List<Showtime> list = showtimeRepo.findOnSaleFrom(Instant.now());
        return ResponseEntity.ok(ApiResponse.success(list.stream().map(this::toDto).toList()));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<ApiResponse<List<SeatStatusDto>>> getSeatStatus(@PathVariable UUID id) {
        List<SeatStatusEntry> seats = appService.getSeatStatus(id);
        return ResponseEntity.ok(ApiResponse.success(seats.stream().map(this::toSeatDto).toList()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShowtimeDto>> create(@RequestBody CreateShowtimeRequest req) {
        List<ShowtimeApplicationService.SeatDefinition> seats = req.seats().stream()
            .map(s -> new ShowtimeApplicationService.SeatDefinition(s.seatId(), s.seatCode(), s.category()))
            .toList();
        Showtime created = appService.createShowtime(
            req.movieId(), req.cinemaId(), req.screenId(),
            req.movieTitle(), req.cinemaName(), req.screenName(),
            req.format(), req.audioType(),
            req.startTime(), req.endTime(),
            req.basePrice(), req.vipPrice(), req.couplePrice(), seats);
        return ResponseEntity.status(201).body(ApiResponse.success(toDto(created)));
    }

    @PatchMapping("/{id}/open-for-sale")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> openForSale(@PathVariable UUID id) {
        appService.openForSale(id);
        return ResponseEntity.ok(ApiResponse.success("Showtime opened for sale"));
    }

    private ShowtimeDto toDto(Showtime s) {
        return new ShowtimeDto(s.getId().toString(), s.getMovieId(), s.getCinemaId(),
            s.getMovieTitle(), s.getCinemaName(), s.getScreenName(), s.getFormat(),
            s.getStartTime(), s.getEndTime(), s.getStatus().name(),
            s.getTotalSeats(), s.getAvailableSeats(), s.getBasePrice());
    }

    private SeatStatusDto toSeatDto(SeatStatusEntry e) {
        return new SeatStatusDto(e.getSeatId(), e.getSeatCode(), e.getCategory().name(),
            e.getStatus().name(), e.getUnitPrice());
    }

    public record ShowtimeDto(String id, UUID movieId, UUID cinemaId,
                               String movieTitle, String cinemaName, String screenName,
                               String format, Instant startTime, Instant endTime,
                               String status, int totalSeats, int availableSeats, long basePrice) {}

    public record SeatStatusDto(UUID seatId, String seatCode, String category,
                                 String status, long unitPrice) {}

    public record CreateShowtimeRequest(
        UUID movieId, UUID cinemaId, UUID screenId,
        String movieTitle, String cinemaName, String screenName,
        String format, String audioType,
        Instant startTime, Instant endTime,
        long basePrice, long vipPrice, long couplePrice,
        List<SeatInput> seats
    ) {}

    public record SeatInput(UUID seatId, String seatCode, SeatCategory category) {}
}
