package com.cinetix.identity.infrastructure.persistence;

import com.cinetix.identity.domain.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileJpaRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByEmail(String email);
}
