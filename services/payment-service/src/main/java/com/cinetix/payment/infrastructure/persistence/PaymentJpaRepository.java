package com.cinetix.payment.infrastructure.persistence;

import com.cinetix.payment.domain.model.Payment;
import com.cinetix.payment.domain.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByBookingId(UUID bookingId);
    Optional<Payment> findByIdempotencyKey(String key);
    List<Payment> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    List<Payment> findByStatus(PaymentStatus status);
}
