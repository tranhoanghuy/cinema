package com.cinetix.booking.infrastructure.messaging.consumer;

import com.cinetix.booking.application.saga.BookingSagaOrchestrator;
import com.cinetix.booking.config.RabbitMQConfig;
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
@Slf4j
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final BookingSagaOrchestrator saga;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_QUEUE)
    public void consume(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(body);

            String eventType = node.path("eventType").asText();
            UUID bookingId = UUID.fromString(node.path("bookingId").asText());

            log.info("Received payment event: type={} bookingId={}", eventType, bookingId);

            switch (eventType) {
                case "payment.events.completed.v1" -> saga.onPaymentConfirmed(bookingId);
                case "payment.events.failed.v1", "payment.events.cancelled.v1" -> {
                    String reason = node.path("reason").asText("Payment failed");
                    saga.onPaymentFailed(bookingId, reason);
                }
                default -> log.warn("Unknown payment event type: {}", eventType);
            }

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("Failed to process payment event: {}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false); // dead-letter
        }
    }
}
