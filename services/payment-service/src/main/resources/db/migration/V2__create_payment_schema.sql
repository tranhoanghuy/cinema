CREATE TABLE payments (
    id                 UUID         NOT NULL DEFAULT gen_random_uuid(),
    booking_id         UUID         NOT NULL,
    customer_id        UUID         NOT NULL,
    amount             BIGINT       NOT NULL,
    currency           VARCHAR(3)   NOT NULL DEFAULT 'VND',
    status             VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    method             VARCHAR(30)  NOT NULL,
    psp_provider       VARCHAR(50),
    payment_url        TEXT,
    psp_transaction_id VARCHAR(255),
    failure_reason     VARCHAR(500),
    idempotency_key    VARCHAR(255) NOT NULL,
    expires_at         TIMESTAMPTZ,
    completed_at       TIMESTAMPTZ,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version            BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT payments_pkey PRIMARY KEY (id),
    CONSTRAINT payments_idempotency_key_unique UNIQUE (idempotency_key),
    CONSTRAINT payments_status_check CHECK (status IN ('PENDING','COMPLETED','FAILED','CANCELLED','REFUNDED','PARTIALLY_REFUNDED')),
    CONSTRAINT payments_amount_check CHECK (amount > 0)
);

CREATE TABLE refunds (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    payment_id      UUID         NOT NULL REFERENCES payments(id),
    booking_id      UUID         NOT NULL,
    amount          BIGINT       NOT NULL,
    currency        VARCHAR(3)   NOT NULL DEFAULT 'VND',
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    reason          VARCHAR(500),
    idempotency_key VARCHAR(255) NOT NULL,
    psp_refund_id   VARCHAR(255),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMPTZ,
    CONSTRAINT refunds_pkey PRIMARY KEY (id),
    CONSTRAINT refunds_idempotency_key_unique UNIQUE (idempotency_key),
    CONSTRAINT refunds_status_check CHECK (status IN ('PENDING','COMPLETED','FAILED'))
);

CREATE INDEX idx_payments_booking_id    ON payments (booking_id);
CREATE INDEX idx_payments_customer_id   ON payments (customer_id, created_at DESC);
CREATE INDEX idx_payments_status        ON payments (status) WHERE status = 'PENDING';
CREATE INDEX idx_refunds_payment_id     ON refunds (payment_id);
