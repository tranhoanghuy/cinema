CREATE TABLE notification_log (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    recipient_id    UUID        NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    channel         VARCHAR(20)  NOT NULL DEFAULT 'EMAIL',
    type            VARCHAR(50)  NOT NULL,
    subject         VARCHAR(500),
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    reference_id    VARCHAR(255),
    error_message   VARCHAR(1000),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    sent_at         TIMESTAMPTZ,
    CONSTRAINT notification_log_pkey PRIMARY KEY (id),
    CONSTRAINT notification_log_status_check CHECK (status IN ('PENDING','SENT','FAILED'))
);

CREATE INDEX idx_notif_recipient ON notification_log (recipient_id, created_at DESC);
CREATE INDEX idx_notif_reference ON notification_log (reference_id);
