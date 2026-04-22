CREATE TABLE tickets (
    id             UUID         NOT NULL DEFAULT gen_random_uuid(),
    booking_id     UUID         NOT NULL,
    customer_id    UUID         NOT NULL,
    seat_id        UUID         NOT NULL,
    seat_code      VARCHAR(20)  NOT NULL,
    showtime_id    UUID         NOT NULL,
    movie_title    VARCHAR(500) NOT NULL,
    cinema_name    VARCHAR(300) NOT NULL,
    screen_name    VARCHAR(100) NOT NULL,
    showtime_start TIMESTAMPTZ  NOT NULL,
    unit_price     BIGINT       NOT NULL,
    currency       VARCHAR(3)   NOT NULL DEFAULT 'VND',
    qr_code        TEXT,
    qr_data        VARCHAR(500),
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    issued_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    used_at        TIMESTAMPTZ,
    CONSTRAINT tickets_pkey PRIMARY KEY (id),
    CONSTRAINT tickets_status_check CHECK (status IN ('ACTIVE','USED','CANCELLED','EXPIRED'))
);

CREATE INDEX idx_tickets_booking_id  ON tickets (booking_id);
CREATE INDEX idx_tickets_customer_id ON tickets (customer_id, issued_at DESC);
CREATE INDEX idx_tickets_showtime    ON tickets (showtime_id, seat_id);
