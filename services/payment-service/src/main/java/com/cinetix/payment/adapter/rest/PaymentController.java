package com.cinetix.payment.adapter.rest;

import com.cinetix.common.web.ApiResponse;
import com.cinetix.payment.application.PaymentApplicationService;
import com.cinetix.payment.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentApplicationService appService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(toDto(appService.getById(id))));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentDto>> getByBooking(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(ApiResponse.success(toDto(appService.getByBookingId(bookingId))));
    }

    /** PSP webhook callback - mark payment as completed */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<PaymentDto>> confirm(
        @PathVariable UUID id,
        @RequestBody Map<String, String> body) {
        String pspTxId = body.getOrDefault("pspTransactionId", "UNKNOWN");
        return ResponseEntity.ok(ApiResponse.success(toDto(appService.confirmPayment(id, pspTxId))));
    }

    /** PSP webhook callback - mark payment as failed */
    @PostMapping("/{id}/fail")
    public ResponseEntity<ApiResponse<PaymentDto>> fail(
        @PathVariable UUID id,
        @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "Payment failed");
        return ResponseEntity.ok(ApiResponse.success(toDto(appService.failPayment(id, reason))));
    }

    private PaymentDto toDto(Payment p) {
        return new PaymentDto(p.getId(), p.getBookingId(), p.getAmount(), p.getCurrency(),
            p.getStatus().name(), p.getMethod().name(), p.getPaymentUrl());
    }

    public record PaymentDto(UUID id, UUID bookingId, long amount, String currency,
                              String status, String method, String paymentUrl) {}
}
