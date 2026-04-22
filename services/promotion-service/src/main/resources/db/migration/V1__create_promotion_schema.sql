CREATE TABLE promotions (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    name             VARCHAR(200) NOT NULL,
    description      VARCHAR(1000),
    discount_type    VARCHAR(20)  NOT NULL,
    discount_percent INT          NOT NULL DEFAULT 0,
    discount_amount  BIGINT       NOT NULL DEFAULT 0,
    max_discount     BIGINT       NOT NULL DEFAULT 0,
    min_order_amount BIGINT       NOT NULL DEFAULT 0,
    cinema_id        UUID,
    movie_id         UUID,
    starts_at        TIMESTAMPTZ  NOT NULL,
    ends_at          TIMESTAMPTZ  NOT NULL,
    max_uses         INT          NOT NULL DEFAULT 2147483647,
    current_uses     INT          NOT NULL DEFAULT 0,
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT promotions_pkey PRIMARY KEY (id),
    CONSTRAINT promotions_discount_type_check CHECK (discount_type IN ('PERCENT','FIXED')),
    CONSTRAINT promotions_times_check CHECK (ends_at > starts_at)
);

CREATE TABLE vouchers (
    id                   UUID         NOT NULL DEFAULT gen_random_uuid(),
    code                 VARCHAR(50)  NOT NULL,
    promotion_id         UUID         NOT NULL REFERENCES promotions(id),
    status               VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    assigned_customer_id UUID,
    booking_id           UUID,
    redemption_id        UUID,
    redeemed_at          TIMESTAMPTZ,
    expires_at           TIMESTAMPTZ  NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT vouchers_pkey PRIMARY KEY (id),
    CONSTRAINT vouchers_code_unique UNIQUE (code),
    CONSTRAINT vouchers_status_check CHECK (status IN ('ACTIVE','REDEEMED','EXPIRED','VOIDED'))
);

CREATE INDEX idx_promotions_active      ON promotions (active, starts_at, ends_at) WHERE active = TRUE;
CREATE INDEX idx_vouchers_code          ON vouchers (code);
CREATE INDEX idx_vouchers_booking_id    ON vouchers (booking_id) WHERE booking_id IS NOT NULL;
CREATE INDEX idx_vouchers_customer      ON vouchers (assigned_customer_id) WHERE assigned_customer_id IS NOT NULL;
