package com.cinetix.promotion.application;

import com.cinetix.common.exception.BusinessException;
import com.cinetix.common.exception.ResourceNotFoundException;
import com.cinetix.promotion.domain.model.*;
import com.cinetix.promotion.infrastructure.persistence.PromotionJpaRepository;
import com.cinetix.promotion.infrastructure.persistence.VoucherJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PromotionApplicationService {

    private final VoucherJpaRepository    voucherRepo;
    private final PromotionJpaRepository  promotionRepo;

    public record ValidationResult(
        UUID voucherId, UUID promotionId, String promotionName,
        DiscountType discountType, int discountPercent,
        long discountAmount, long finalAmount, Instant validUntil
    ) {}

    @Transactional(readOnly = true)
    public ValidationResult validateVoucher(String code, UUID customerId, long orderAmount,
                                             UUID cinemaId, UUID movieId) {
        Voucher voucher = voucherRepo.findByCode(code.toUpperCase())
            .orElseThrow(() -> new BusinessException("VOUCHER_NOT_FOUND", "Voucher not found: " + code));

        if (!voucher.isUsable(customerId, Instant.now()))
            throw new BusinessException("VOUCHER_NOT_USABLE",
                "Voucher is not usable: status=" + voucher.getStatus());

        Promotion promotion = promotionRepo.findById(voucher.getPromotionId())
            .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));

        if (!promotion.isValid(Instant.now()))
            throw new BusinessException("PROMOTION_EXPIRED", "Promotion has expired or reached max uses");

        long discount   = promotion.computeDiscount(orderAmount);
        long finalAmount = Math.max(0, orderAmount - discount);

        return new ValidationResult(
            voucher.getId(), promotion.getId(), promotion.getName(),
            promotion.getDiscountType(), promotion.getDiscountPercent(),
            discount, finalAmount, voucher.getExpiresAt()
        );
    }

    public UUID redeemVoucher(String code, UUID voucherId, UUID customerId,
                               UUID bookingId, long discountApplied, String idempotencyKey) {
        Voucher voucher = voucherRepo.findById(voucherId)
            .orElseThrow(() -> new ResourceNotFoundException("Voucher not found: " + voucherId));

        // Idempotency - already redeemed for this booking
        if (voucher.getBookingId() != null && voucher.getBookingId().equals(bookingId))
            return voucher.getRedemptionId();

        if (!voucher.isUsable(customerId, Instant.now()))
            throw new BusinessException("VOUCHER_NOT_USABLE", "Voucher is not usable");

        UUID redemptionId = UUID.randomUUID();
        voucher.redeem(bookingId, redemptionId);
        voucherRepo.save(voucher);

        // Increment promotion usage
        promotionRepo.findById(voucher.getPromotionId()).ifPresent(p -> {
            p.setCurrentUses(p.getCurrentUses() + 1);
            promotionRepo.save(p);
        });

        log.info("Voucher redeemed: code={} bookingId={} redemptionId={}", code, bookingId, redemptionId);
        return redemptionId;
    }

    public void voidVoucher(UUID voucherId, UUID bookingId, String idempotencyKey) {
        Voucher voucher = voucherRepo.findByBookingId(bookingId).orElse(null);
        if (voucher == null) {
            log.warn("No voucher found for bookingId={} to void", bookingId);
            return;
        }

        try {
            voucher.voidVoucher();
            voucherRepo.save(voucher);
            promotionRepo.findById(voucher.getPromotionId()).ifPresent(p -> {
                p.setCurrentUses(Math.max(0, p.getCurrentUses() - 1));
                promotionRepo.save(p);
            });
            log.info("Voucher voided: voucherId={} bookingId={}", voucher.getId(), bookingId);
        } catch (IllegalStateException e) {
            log.warn("Cannot void voucher {}: {}", voucher.getId(), e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Promotion> listActivePromotions(UUID cinemaId, UUID movieId) {
        return promotionRepo.findActivePromotions(Instant.now(), cinemaId, movieId);
    }

    public Promotion createPromotion(Promotion promotion) {
        return promotionRepo.save(promotion);
    }

    public Voucher createVoucher(String code, UUID promotionId, UUID assignedCustomerId, Instant expiresAt) {
        if (voucherRepo.findByCode(code.toUpperCase()).isPresent())
            throw new BusinessException("VOUCHER_CODE_EXISTS", "Voucher code already exists: " + code);

        Voucher voucher = Voucher.builder()
            .code(code.toUpperCase())
            .promotionId(promotionId)
            .assignedCustomerId(assignedCustomerId)
            .expiresAt(expiresAt)
            .build();
        return voucherRepo.save(voucher);
    }
}
