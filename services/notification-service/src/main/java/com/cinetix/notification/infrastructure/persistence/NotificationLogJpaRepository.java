package com.cinetix.notification.infrastructure.persistence;

import com.cinetix.notification.domain.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationLogJpaRepository extends JpaRepository<NotificationLog, UUID> {
    List<NotificationLog> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);
    List<NotificationLog> findByReferenceId(String referenceId);
}
