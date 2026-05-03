CREATE TABLE cinemas (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    name       VARCHAR(300) NOT NULL,
    address    VARCHAR(500) NOT NULL,
    city       VARCHAR(100) NOT NULL,
    district   VARCHAR(100),
    phone      VARCHAR(20),
    latitude   DOUBLE PRECISION,
    longitude  DOUBLE PRECISION,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT cinemas_pkey PRIMARY KEY (id)
);

CREATE TABLE screens (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    cinema_id   UUID        NOT NULL REFERENCES cinemas(id),
    name        VARCHAR(100) NOT NULL,
    type        VARCHAR(20)  NOT NULL DEFAULT 'STANDARD',
    total_seats INT          NOT NULL,
    rows        INT          NOT NULL,
    columns     INT          NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT screens_pkey PRIMARY KEY (id),
    CONSTRAINT screens_type_check CHECK (type IN ('STANDARD','IMAX','FOUR_DX','SCREENX','GOLD_CLASS'))
);

CREATE TABLE seats (
    id        UUID        NOT NULL DEFAULT gen_random_uuid(),
    screen_id UUID        NOT NULL REFERENCES screens(id),
    seat_code VARCHAR(20) NOT NULL,
    row_num   INT         NOT NULL,
    col_num   INT         NOT NULL,
    category  VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
    active    BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT seats_pkey PRIMARY KEY (id),
    CONSTRAINT seats_screen_code_unique UNIQUE (screen_id, seat_code),
    CONSTRAINT seats_category_check CHECK (category IN ('STANDARD','VIP','COUPLE'))
);

CREATE INDEX idx_cinemas_city     ON cinemas (city) WHERE active = TRUE;
CREATE INDEX idx_screens_cinema   ON screens (cinema_id) WHERE active = TRUE;
CREATE INDEX idx_seats_screen     ON seats (screen_id, category);
