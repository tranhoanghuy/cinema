package com.cinetix.showtime.domain.model;

import com.cinetix.common.domain.AggregateRoot;
import com.cinetix.showtime.domain.model.valueobject.ShowtimeId;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "showtimes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Showtime extends AggregateRoot<ShowtimeId> {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "id"))
    private ShowtimeId id;

    @Column(name = "movie_id", nullable = false)
    private UUID movieId;

    @Column(name = "cinema_id", nullable = false)
    private UUID cinemaId;

    @Column(name = "screen_id", nullable = false)
    private UUID screenId;

    @Column(name = "movie_title", nullable = false, length = 500)
    private String movieTitle;

    @Column(name = "cinema_name", nullable = false, length = 300)
    private String cinemaName;

    @Column(name = "screen_name", nullable = false, length = 100)
    private String screenName;

    @Column(name = "format", nullable = false, length = 20)
    private String format = "2D";

    @Column(name = "audio_type", nullable = false, length = 20)
    private String audioType = "ORIGINAL";

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ShowtimeStatus status = ShowtimeStatus.SCHEDULED;

    @Column(name = "base_price", nullable = false)
    private long basePrice;

    @Column(name = "vip_price", nullable = false)
    private long vipPrice;

    @Column(name = "couple_price", nullable = false)
    private long couplePrice;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "VND";

    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    @Column(name = "available_seats", nullable = false)
    private int availableSeats;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public static Showtime create(UUID movieId, UUID cinemaId, UUID screenId,
                                   String movieTitle, String cinemaName, String screenName,
                                   String format, String audioType,
                                   Instant startTime, Instant endTime,
                                   long basePrice, long vipPrice, long couplePrice,
                                   int totalSeats) {
        var s = new Showtime();
        s.id = ShowtimeId.generate();
        s.movieId = movieId;
        s.cinemaId = cinemaId;
        s.screenId = screenId;
        s.movieTitle = movieTitle;
        s.cinemaName = cinemaName;
        s.screenName = screenName;
        s.format = format;
        s.audioType = audioType;
        s.startTime = startTime;
        s.endTime = endTime;
        s.status = ShowtimeStatus.SCHEDULED;
        s.basePrice = basePrice;
        s.vipPrice = vipPrice;
        s.couplePrice = couplePrice;
        s.totalSeats = totalSeats;
        s.availableSeats = totalSeats;
        s.createdAt = Instant.now();
        s.updatedAt = Instant.now();
        return s;
    }

    public void openForSale() {
        if (status != ShowtimeStatus.SCHEDULED)
            throw new IllegalStateException("Cannot open showtime in status: " + status);
        this.status = ShowtimeStatus.ON_SALE;
        this.updatedAt = Instant.now();
    }

    public void decrementAvailableSeats(int count) {
        if (availableSeats < count)
            throw new IllegalStateException("Not enough available seats");
        this.availableSeats -= count;
        if (availableSeats == 0) this.status = ShowtimeStatus.FULL;
        this.updatedAt = Instant.now();
    }

    public void incrementAvailableSeats(int count) {
        this.availableSeats = Math.min(totalSeats, availableSeats + count);
        if (status == ShowtimeStatus.FULL && availableSeats > 0) this.status = ShowtimeStatus.ON_SALE;
        this.updatedAt = Instant.now();
    }

    public long priceForCategory(SeatCategory category) {
        return switch (category) {
            case VIP -> vipPrice;
            case COUPLE -> couplePrice;
            default -> basePrice;
        };
    }

    @Override public ShowtimeId getId() { return id; }
}
