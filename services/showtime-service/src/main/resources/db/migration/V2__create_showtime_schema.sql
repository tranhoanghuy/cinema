CREATE TABLE showtimes (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    movie_id        UUID         NOT NULL,
    cinema_id       UUID         NOT NULL,
    screen_id       UUID         NOT NULL,
    movie_title     VARCHAR(500) NOT NULL,
    cinema_name     VARCHAR(300) NOT NULL,
    screen_name     VARCHAR(100) NOT NULL,
    format          VARCHAR(20)  NOT NULL DEFAULT '2D',
    audio_type      VARCHAR(20)  NOT NULL DEFAULT 'ORIGINAL',
    start_time      TIMESTAMPTZ  NOT NULL,
    end_time        TIMESTAMPTZ  NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
    base_price      BIGINT       NOT NULL,
    vip_price       BIGINT       NOT NULL,
    couple_price    BIGINT       NOT NULL,
    currency        VARCHAR(3)   NOT NULL DEFAULT 'VND',
    total_seats     INT          NOT NULL,
    available_seats INT          NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version         BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT showtimes_pkey PRIMARY KEY (id),
    CONSTRAINT showtimes_status_check CHECK (status IN ('SCHEDULED','ON_SALE','FULL','CANCELLED','COMPLETED')),
    CONSTRAINT showtimes_times_check CHECK (end_time > start_time)
);

CREATE TABLE seat_status (
    id           UUID        NOT NULL DEFAULT gen_random_uuid(),
    showtime_id  UUID        NOT NULL REFERENCES showtimes(id),
    seat_id      UUID        NOT NULL,
    seat_code    VARCHAR(20) NOT NULL,
    category     VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
    status       VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    booking_id   UUID,
    confirmed_at TIMESTAMPTZ,
    unit_price   BIGINT      NOT NULL,
    CONSTRAINT seat_status_pkey PRIMARY KEY (id),
    CONSTRAINT seat_status_unique UNIQUE (showtime_id, seat_id),
    CONSTRAINT seat_status_check CHECK (status IN ('AVAILABLE','HELD','BOOKED','UNAVAILABLE'))
);

-- Indexes
CREATE INDEX idx_showtimes_movie_status    ON showtimes (movie_id, status, start_time);
CREATE INDEX idx_showtimes_cinema_time     ON showtimes (cinema_id, start_time);
CREATE INDEX idx_showtimes_on_sale         ON showtimes (status, start_time) WHERE status = 'ON_SALE';
CREATE INDEX idx_seat_status_showtime      ON seat_status (showtime_id, status);
CREATE INDEX idx_seat_status_booking       ON seat_status (booking_id) WHERE booking_id IS NOT NULL;
