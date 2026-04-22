package com.cinetix.ticket.infrastructure.messaging;

import com.cinetix.ticket.application.TicketApplicationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventConsumer {

    private final TicketApplicationService appService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "ticket-svc.booking.events")
    public void consume(Message message, Channel channel) throws Exception {
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(body);
            String eventType = node.path("eventType").asText();

            if ("booking.events.confirmed.v1".equals(eventType)) {
                handleBookingConfirmed(node);
            } else if ("booking.events.cancelled.v1".equals(eventType)) {
                UUID bookingId = UUID.fromString(node.path("aggregateId").path("value").asText());
                appService.cancelTicketsByBooking(bookingId);
            }
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed to process booking event: {}", e.getMessage(), e);
            channel.basicNack(tag, false, false);
        }
    }

    private void handleBookingConfirmed(JsonNode node) {
        UUID bookingId  = UUID.fromString(node.path("aggregateId").path("value").asText());
        UUID customerId = UUID.fromString(node.path("customerId").path("value").asText());
        UUID showtimeId = UUID.fromString(node.path("showtimeId").path("value").asText());
        Instant start   = Instant.now(); // Will be enriched in real implementation via showtime query
        long amount = node.path("finalAmount").path("amount").asLong(0);

        JsonNode seatIds = node.path("seatIds");
        List<TicketApplicationService.TicketIssueCommand> commands = new ArrayList<>();
        seatIds.forEach(seatNode -> {
            UUID seatId = UUID.fromString(seatNode.path("value").asText());
            commands.add(new TicketApplicationService.TicketIssueCommand(
                bookingId, customerId, seatId, "N/A",
                showtimeId, "Movie", "Cinema", "Screen",
                start, amount / Math.max(1, seatIds.size())
            ));
        });

        if (!commands.isEmpty()) appService.issueTickets(commands);
    }
}
