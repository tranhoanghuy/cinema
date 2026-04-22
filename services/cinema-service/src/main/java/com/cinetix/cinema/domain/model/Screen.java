package com.cinetix.cinema.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "screens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_id", nullable = false)
    private Cinema cinema;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private ScreenType type = ScreenType.STANDARD;

    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    @Column(name = "rows", nullable = false)
    private int rows;

    @Column(name = "columns", nullable = false)
    private int columns;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Seat> seats = new ArrayList<>();
}
