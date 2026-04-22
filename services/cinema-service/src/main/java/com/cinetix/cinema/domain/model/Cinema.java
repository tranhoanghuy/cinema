package com.cinetix.cinema.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cinemas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cinema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 300)
    private String name;

    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "cinema", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Screen> screens = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
