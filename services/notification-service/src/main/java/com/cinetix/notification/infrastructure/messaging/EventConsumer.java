package com.cinetix.notification.infrastructure.messaging;

import com.cinetix.notification.application.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {

    private final NotificationService appService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "notification-svc.events")
    public void consume(Message message, Channel channel) throws Exception {
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(body);
            String eventType = node.path("eventType").asText();

            // In a real scenario, customer email would be fetched from identity-service
            // For simplicity, we use placeholder email from the event if present
            String email = node.path("customerEmail").asText("user@example.com");
            UUID customerId = parseUUID(node, "customerId");

            switch (eventType) {
                case "booking.events.confirmed.v1" -> {
                    String bookingId = node.path("aggregateId").path("value").asText();
                    appService.sendBookingConfirmation(customerId, email, bookingId,
                        "Movie", "Cinema", "Showtime");
                }
                case "booking.events.cancelled.v1" -> {
                    String bookingId = node.path("aggregateId").path("value").asText();
                    String reason = node.path("reason").asText("No reason provided");
                    appService.sendBookingCancellation(customerId, email, bookingId, reason);
                }
                case "payment.events.failed.v1" -> {
                    String bookingId = node.path("bookingId").asText();
                    String reason = node.path("reason").asText("Payment failed");
                    appService.sendPaymentFailed(customerId, email, bookingId, reason);
                }
                default -> log.debug("Ignoring event type: {}", eventType);
            }

            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Event processing failed: {}", e.getMessage(), e);
            channel.basicNack(tag, false, false);
        }
    }

    private UUID parseUUID(JsonNode node, String field) {
        try {
            String val = node.path(field).path("value").asText();
            if (val.isEmpty()) val = node.path(field).asText();
            return UUID.fromString(val);
        } catch (Exception e) {
            return UUID.randomUUID();
        }
    }
}
