package com.cinetix.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxJpaRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query(value = """
        SELECT o FROM OutboxEvent o
        WHERE o.status = 'PENDING'
        ORDER BY o.createdAt ASC
        LIMIT :limit
        """)
    List<OutboxEvent> findPendingBatch(@Param("limit") int limit);

    @Modifying
    @Query("UPDATE OutboxEvent o SET o.status = 'PROCESSED', o.processedAt = :at WHERE o.id = :id")
    void markProcessed(@Param("id") UUID id, @Param("at") Instant at);

    @Modifying
    @Query("""
        UPDATE OutboxEvent o
        SET o.retryCount = o.retryCount + 1, o.errorMessage = :error
        WHERE o.id = :id
        """)
    void incrementRetry(@Param("id") UUID id, @Param("error") String error);

    @Modifying
    @Query("UPDATE OutboxEvent o SET o.status = 'FAILED', o.errorMessage = :error WHERE o.id = :id")
    void markFailed(@Param("id") UUID id, @Param("error") String error);
}
