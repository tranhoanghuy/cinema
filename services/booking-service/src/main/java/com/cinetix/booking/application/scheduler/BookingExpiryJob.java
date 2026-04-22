package com.cinetix.booking.application.scheduler;

import com.cinetix.booking.application.saga.BookingSagaOrchestrator;
import com.cinetix.booking.domain.model.Booking;
import com.cinetix.booking.domain.port.outbound.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BookingExpiryJob {

    private final BookingRepository bookingRepository;
    private final BookingSagaOrchestrator saga;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${cinetix.booking.expiry-check-interval-ms:60000}")
    public void checkExpiredBookings() {
        List<Booking> expired = bookingRepository.findExpiredPendingBookings(Instant.now(clock), 50);
        log.debug("Found {} expired bookings", expired.size());

        for (Booking booking : expired) {
            try {
                log.info("Expiring booking {}", booking.getId());
                saga.onPaymentTimeout(booking.getId().value());
            } catch (Exception e) {
                log.error("Failed to expire booking {}: {}", booking.getId(), e.getMessage());
            }
        }
    }
}
