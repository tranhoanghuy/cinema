package com.cinetix.showtime.infrastructure.messaging;

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
public class OutboxEventPublisherAdapter {

    private final OutboxJpaRepository outboxRepo;
    private final RabbitTemplate      rabbitTemplate;
    private final ObjectMapper        objectMapper;

    private static final String EXCHANGE   = "cinetix.events";
    private static final int    BATCH      = 100;
    private static final int    MAX_RETRY  = 5;

    @Transactional(propagation = Propagation.MANDATORY)
    public void publishAll(List<DomainEvent> events) {
        events.forEach(event -> {
            var outbox = OutboxEvent.builder()
                .aggregateType(event.getAggregateType())
                .aggregateId(event.getAggregateId().toString())
                .eventType(event.getEventType())
                .payload(objectMapper.valueToTree(event))
                .metadata(buildMeta())
                .build();
            outboxRepo.save(outbox);
        });
    }

    @Scheduled(fixedDelayString = "${cinetix.outbox.relay-interval-ms:500}")
    @Transactional
    public void relay() {
        var pending = outboxRepo.findPendingBatch(BATCH);
        if (pending.isEmpty()) return;
        pending.forEach(event -> {
            try {
                rabbitTemplate.convertAndSend(EXCHANGE, event.getEventType(),
                    event.getPayload().toString(), msg -> {
                        msg.getMessageProperties().setMessageId(event.getId().toString());
                        msg.getMessageProperties().setContentType("application/json");
                        return msg;
                    });
                outboxRepo.markProcessed(event.getId(), Instant.now());
            } catch (Exception e) {
                log.error("Relay failed for outbox event {}: {}", event.getId(), e.getMessage());
                outboxRepo.incrementRetry(event.getId(), e.getMessage());
                if (event.getRetryCount() + 1 >= MAX_RETRY) {
                    outboxRepo.markFailed(event.getId(), e.getMessage());
                }
            }
        });
    }

    private ObjectNode buildMeta() {
        var meta = objectMapper.createObjectNode();
        meta.put("traceId", MDC.get("traceId") != null ? MDC.get("traceId") : "");
        return meta;
    }
}
