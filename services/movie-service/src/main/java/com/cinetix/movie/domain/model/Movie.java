package com.cinetix.movie.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "original_title", length = 500)
    private String originalTitle;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "rating", length = 10)
    private String rating;

    @Column(name = "language", length = 50)
    private String language;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    @Column(name = "backdrop_url", length = 500)
    private String backdropUrl;

    @Column(name = "trailer_url", length = 500)
    private String trailerUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private MovieStatus status = MovieStatus.COMING_SOON;

    @ElementCollection
    @CollectionTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "genre")
    @Builder.Default
    private List<String> genres = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "movie_formats", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "format")
    @Builder.Default
    private List<String> formats = new ArrayList<>();

    @Column(name = "director", length = 200)
    private String director;

    @Column(name = "cast_list", columnDefinition = "TEXT")
    private String castList;

    @Column(name = "age_rating", length = 10)
    private String ageRating;

    @Column(name = "imdb_score")
    private Double imdbScore;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    public boolean isNowShowing() {
        return status == MovieStatus.NOW_SHOWING;
    }
}
