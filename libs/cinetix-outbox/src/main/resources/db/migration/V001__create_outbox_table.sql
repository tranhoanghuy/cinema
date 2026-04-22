CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE outbox_events (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    aggregate_type  VARCHAR(100)    NOT NULL,
    aggregate_id    VARCHAR(255)    NOT NULL,
    event_type      VARCHAR(200)    NOT NULL,
    payload         JSONB           NOT NULL,
    metadata        JSONB           NOT NULL DEFAULT '{}',
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    processed_at    TIMESTAMPTZ,
    retry_count     INT             NOT NULL DEFAULT 0,
    error_message   TEXT,
    CONSTRAINT outbox_events_pkey PRIMARY KEY (id),
    CONSTRAINT outbox_events_status_check CHECK (status IN ('PENDING','PROCESSED','FAILED'))
);

CREATE INDEX idx_outbox_status_created ON outbox_events (status, created_at)
    WHERE status = 'PENDING';
