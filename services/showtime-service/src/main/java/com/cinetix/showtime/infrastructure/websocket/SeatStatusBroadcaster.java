package com.cinetix.showtime.infrastructure.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Broadcasts seat status changes to WebSocket subscribers.
 * Topic: /topic/showtime/{showtimeId}/seats
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SeatStatusBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastSeatUpdate(UUID showtimeId, List<String> seatIds, String status) {
        var payload = Map.of(
            "showtimeId", showtimeId.toString(),
            "seatIds", seatIds,
            "status", status,
            "timestamp", System.currentTimeMillis()
        );
        String destination = "/topic/showtime/" + showtimeId + "/seats";
        messagingTemplate.convertAndSend(destination, payload);
        log.debug("Broadcasted seat update: showtimeId={} seats={} status={}", showtimeId, seatIds, status);
    }
}
