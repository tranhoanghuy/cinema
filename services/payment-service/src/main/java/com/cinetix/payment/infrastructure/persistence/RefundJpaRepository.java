package com.cinetix.payment.infrastructure.persistence;

import com.cinetix.payment.domain.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefundJpaRepository extends JpaRepository<Refund, UUID> {
    Optional<Refund> findByIdempotencyKey(String key);
    Optional<Refund> findByPaymentId(UUID paymentId);
}
