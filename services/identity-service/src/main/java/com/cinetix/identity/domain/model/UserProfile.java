package com.cinetix.identity.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Local user profile that mirrors Keycloak user data.
 * Keycloak is the primary IAM; this stores additional application-specific attributes.
 */
@Entity
@Table(name = "user_profiles",
    uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @Column(name = "id")
    private UUID id;  // Same as Keycloak sub claim

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    public void update(String fullName, String phone, String avatarUrl) {
        if (fullName != null) this.fullName = fullName;
        if (phone != null)    this.phone = phone;
        if (avatarUrl != null) this.avatarUrl = avatarUrl;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status = UserStatus.DEACTIVATED;
        this.updatedAt = Instant.now();
    }
}
