package com.cinetix.ticket.adapter.rest;

import com.cinetix.common.web.ApiResponse;
import com.cinetix.ticket.application.TicketApplicationService;
import com.cinetix.ticket.domain.model.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketApplicationService appService;

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<List<Ticket>>> getByBooking(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(ApiResponse.success(appService.getByBookingId(bookingId)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Ticket>>> getMyTickets(@AuthenticationPrincipal Jwt jwt) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success(appService.getByCustomerId(customerId)));
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<ApiResponse<Ticket>> validate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(appService.validateAndUse(id)));
    }
}
