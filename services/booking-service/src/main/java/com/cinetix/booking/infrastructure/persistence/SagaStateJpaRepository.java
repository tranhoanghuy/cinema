package com.cinetix.booking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SagaStateJpaRepository extends JpaRepository<SagaStateEntity, UUID> {
}
