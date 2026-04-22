package com.cinetix.booking.infrastructure.messaging;

import com.cinetix.booking.domain.port.outbound.EventPublisherPort;
import com.cinetix.common.domain.DomainEvent;
import com.cinetix.outbox.OutboxEvent;
import com.cinetix.outbox.OutboxJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisherAdapter implements EventPublisherPort {

    private final OutboxJpaRepository outboxRepo;
    private final RabbitTemplate      rabbitTemplate;
    private final ObjectMapper        objectMapper;

    private static final String EXCHANGE    = "cinetix.events";
    private static final int    BATCH_SIZE  = 100;
    private static final int    MAX_RETRIES = 5;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publishAll(List<DomainEvent> events) {
        events.forEach(event -> {
            try {
                var outbox = OutboxEvent.builder()
                    .aggregateType(event.getAggregateType())
                    .aggregateId(event.getAggregateId().toString())
                    .eventType(event.getEventType())
                    .payload(objectMapper.valueToTree(event))
                    .metadata(buildMeta())
                    .build();
                outboxRepo.save(outbox);
            } catch (Exception e) {
                throw new RuntimeException("Failed to write outbox event: " + event.getEventType(), e);
            }
        });
    }

    @Scheduled(fixedDelayString = "${cinetix.outbox.relay-interval-ms:500}")
    @Transactional
    public void relay() {
        var pending = outboxRepo.findPendingBatch(BATCH_SIZE);
        if (pending.isEmpty()) return;

        pending.forEach(event -> {
            try {
                rabbitTemplate.convertAndSend(EXCHANGE, event.getEventType(),
                    event.getPayload().toString(), msg -> {
                        msg.getMessageProperties().setMessageId(event.getId().toString());
                        msg.getMessageProperties().setContentType("application/json");
                        msg.getMessageProperties().setHeader("eventType", event.getEventType());
                        return msg;
                    });
                outboxRepo.markProcessed(event.getId(), Instant.now());
            } catch (Exception e) {
                log.error("Failed to relay outbox event {}: {}", event.getId(), e.getMessage());
                outboxRepo.incrementRetry(event.getId(), e.getMessage());
                if (event.getRetryCount() + 1 >= MAX_RETRIES) {
                    outboxRepo.markFailed(event.getId(), e.getMessage());
                }
            }
        });
    }

    private ObjectNode buildMeta() {
        var meta = objectMapper.createObjectNode();
        meta.put("traceId", MDC.get("traceId") != null ? MDC.get("traceId") : "");
        meta.put("spanId",  MDC.get("spanId")  != null ? MDC.get("spanId")  : "");
        return meta;
    }
}
