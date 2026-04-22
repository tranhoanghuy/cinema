package com.cinetix.notification.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId;

    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;

    @Column(name = "channel", nullable = false, length = 20)
    private String channel;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "subject", length = 500)
    private String subject;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "reference_id", length = 255)
    private String referenceId;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "sent_at")
    private Instant sentAt;

    public void markSent() {
        this.status = "SENT";
        this.sentAt = Instant.now();
    }

    public void markFailed(String error) {
        this.status = "FAILED";
        this.errorMessage = error;
    }
}
