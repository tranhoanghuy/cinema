package com.cinetix.promotion.infrastructure.persistence;

import com.cinetix.promotion.domain.model.Voucher;
import com.cinetix.promotion.domain.model.VoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VoucherJpaRepository extends JpaRepository<Voucher, UUID> {
    Optional<Voucher> findByCode(String code);
    Optional<Voucher> findByCodeAndStatus(String code, VoucherStatus status);
    Optional<Voucher> findByBookingId(UUID bookingId);
}
