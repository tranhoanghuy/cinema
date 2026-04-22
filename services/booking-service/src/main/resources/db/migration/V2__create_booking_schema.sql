CREATE TABLE bookings (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    customer_id      UUID         NOT NULL,
    showtime_id      UUID         NOT NULL,
    cinema_id        UUID         NOT NULL,
    movie_id         UUID         NOT NULL,
    status           VARCHAR(30)  NOT NULL DEFAULT 'INITIATED',
    subtotal         BIGINT       NOT NULL,
    discount_amount  BIGINT       NOT NULL DEFAULT 0,
    final_amount     BIGINT       NOT NULL,
    currency         VARCHAR(3)   NOT NULL DEFAULT 'VND',
    voucher_code     VARCHAR(100),
    payment_id       VARCHAR(255),
    payment_method   VARCHAR(30),
    payment_url      TEXT,
    expires_at       TIMESTAMPTZ  NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version          BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT bookings_pkey PRIMARY KEY (id),
    CONSTRAINT bookings_status_check CHECK (status IN (
        'INITIATED','SEATS_HELD','VOUCHER_APPLIED','PAYMENT_PENDING',
        'CONFIRMING','CONFIRMED','COMPENSATING','CANCELLED','FAILED'
    )),
    CONSTRAINT bookings_amount_check CHECK (final_amount >= 0 AND subtotal > 0)
);

CREATE TABLE booking_items (
    id             UUID        NOT NULL DEFAULT gen_random_uuid(),
    booking_id     UUID        NOT NULL REFERENCES bookings(id),
    seat_id        UUID        NOT NULL,
    seat_code      VARCHAR(20) NOT NULL,
    seat_category  VARCHAR(20) NOT NULL,
    unit_price     BIGINT      NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT booking_items_pkey PRIMARY KEY (id),
    CONSTRAINT booking_items_booking_seat_unique UNIQUE (booking_id, seat_id)
);

CREATE TABLE saga_states (
    booking_id          UUID        NOT NULL,
    current_step        VARCHAR(50) NOT NULL,
    started_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at        TIMESTAMPTZ,
    compensation_reason TEXT,
    step_history        JSONB       NOT NULL DEFAULT '[]',
    CONSTRAINT saga_states_pkey PRIMARY KEY (booking_id)
);

CREATE TABLE booking_read_model (
    id               UUID          NOT NULL,
    customer_id      UUID          NOT NULL,
    movie_title      VARCHAR(500),
    movie_poster_url TEXT,
    cinema_name      VARCHAR(300),
    screen_name      VARCHAR(100),
    showtime_start   TIMESTAMPTZ,
    seat_codes       TEXT,
    total_amount     BIGINT,
    status           VARCHAR(30),
    voucher_code     VARCHAR(100),
    payment_method   VARCHAR(50),
    payment_url      TEXT,
    qr_codes         TEXT,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT booking_read_model_pkey PRIMARY KEY (id)
);

CREATE TABLE processed_events (
    event_id       UUID         NOT NULL,
    consumer_group VARCHAR(100) NOT NULL,
    processed_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT processed_events_pkey PRIMARY KEY (event_id, consumer_group)
);

-- Indexes
CREATE INDEX idx_bookings_customer_status ON bookings (customer_id, created_at DESC)
    WHERE status NOT IN ('FAILED');
CREATE INDEX idx_bookings_expiry ON bookings (expires_at)
    WHERE status = 'PAYMENT_PENDING';
CREATE INDEX idx_booking_items_booking_id ON booking_items (booking_id);
CREATE INDEX idx_booking_read_model_customer ON booking_read_model (customer_id, created_at DESC);
CREATE INDEX idx_processed_events_age ON processed_events (processed_at);
