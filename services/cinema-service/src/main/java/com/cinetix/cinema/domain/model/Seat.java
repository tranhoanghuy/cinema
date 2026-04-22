package com.cinetix.cinema.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "seats",
    uniqueConstraints = @UniqueConstraint(columnNames = {"screen_id", "seat_code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Column(name = "seat_code", nullable = false, length = 20)
    private String seatCode;

    @Column(name = "row_num", nullable = false)
    private int rowNum;

    @Column(name = "col_num", nullable = false)
    private int colNum;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    @Builder.Default
    private SeatCategory category = SeatCategory.STANDARD;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
