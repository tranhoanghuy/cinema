package com.cinetix.showtime.application;

import com.cinetix.showtime.domain.model.*;
import com.cinetix.showtime.domain.model.valueobject.ShowtimeId;
import com.cinetix.showtime.infrastructure.persistence.SeatStatusJpaRepository;
import com.cinetix.showtime.infrastructure.persistence.ShowtimeJpaRepository;
import com.cinetix.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ShowtimeApplicationService {

    private final ShowtimeJpaRepository   showtimeRepo;
    private final SeatStatusJpaRepository seatStatusRepo;

    public Showtime createShowtime(UUID movieId, UUID cinemaId, UUID screenId,
                                    String movieTitle, String cinemaName, String screenName,
                                    String format, String audioType,
                                    Instant startTime, Instant endTime,
                                    long basePrice, long vipPrice, long couplePrice,
                                    List<SeatDefinition> seats) {
        Showtime showtime = Showtime.create(movieId, cinemaId, screenId, movieTitle, cinemaName,
            screenName, format, audioType, startTime, endTime,
            basePrice, vipPrice, couplePrice, seats.size());
        showtimeRepo.save(showtime);

        List<SeatStatusEntry> entries = new ArrayList<>();
        for (SeatDefinition seat : seats) {
            long price = showtime.priceForCategory(seat.category());
            entries.add(SeatStatusEntry.builder()
                .showtimeId(showtime.getId().value())
                .seatId(seat.seatId())
                .seatCode(seat.seatCode())
                .category(seat.category())
                .status(SeatOccupancyStatus.AVAILABLE)
                .unitPrice(price)
                .build());
        }
        seatStatusRepo.saveAll(entries);
        return showtime;
    }

    public void openForSale(UUID showtimeId) {
        Showtime s = showtimeRepo.findById(ShowtimeId.of(showtimeId))
            .orElseThrow(() -> new ResourceNotFoundException("Showtime not found: " + showtimeId));
        s.openForSale();
        showtimeRepo.save(s);
    }

    @Transactional(readOnly = true)
    public Showtime getById(UUID showtimeId) {
        return showtimeRepo.findById(ShowtimeId.of(showtimeId))
            .orElseThrow(() -> new ResourceNotFoundException("Showtime not found: " + showtimeId));
    }

    @Transactional(readOnly = true)
    public List<SeatStatusEntry> getSeatStatus(UUID showtimeId) {
        return seatStatusRepo.findByShowtimeId(showtimeId);
    }

    public record SeatDefinition(UUID seatId, String seatCode, SeatCategory category) {}
}
