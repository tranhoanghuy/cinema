package com.cinetix.promotion.adapter.rest;

import com.cinetix.common.web.ApiResponse;
import com.cinetix.promotion.application.PromotionApplicationService;
import com.cinetix.promotion.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionApplicationService appService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Promotion>>> listActive(
        @RequestParam(required = false) UUID cinemaId,
        @RequestParam(required = false) UUID movieId) {
        return ResponseEntity.ok(ApiResponse.success(appService.listActivePromotions(cinemaId, movieId)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Promotion>> create(@RequestBody Promotion promotion) {
        return ResponseEntity.status(201).body(ApiResponse.success(appService.createPromotion(promotion)));
    }

    @PostMapping("/vouchers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Voucher>> createVoucher(@RequestBody CreateVoucherRequest req) {
        return ResponseEntity.status(201).body(ApiResponse.success(
            appService.createVoucher(req.code(), req.promotionId(), req.assignedCustomerId(), req.expiresAt())
        ));
    }

    public record CreateVoucherRequest(String code, UUID promotionId,
                                        UUID assignedCustomerId, Instant expiresAt) {}
}
