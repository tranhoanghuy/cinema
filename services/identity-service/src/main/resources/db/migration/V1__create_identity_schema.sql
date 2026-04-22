CREATE TABLE user_profiles (
    id          UUID         NOT NULL,
    email       VARCHAR(255) NOT NULL,
    full_name   VARCHAR(200) NOT NULL,
    phone       VARCHAR(20),
    avatar_url  VARCHAR(500),
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT user_profiles_pkey PRIMARY KEY (id),
    CONSTRAINT user_profiles_email_unique UNIQUE (email),
    CONSTRAINT user_profiles_status_check CHECK (status IN ('ACTIVE','DEACTIVATED','SUSPENDED'))
);

CREATE INDEX idx_user_profiles_email ON user_profiles (email);
