CREATE TABLE conversations (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    customer_id UUID        NOT NULL,
    agent_id    UUID,
    status      VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    subject     VARCHAR(200),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at   TIMESTAMPTZ,
    CONSTRAINT conversations_pkey PRIMARY KEY (id),
    CONSTRAINT conversations_status_check CHECK (status IN ('OPEN','ASSIGNED','CLOSED'))
);

CREATE TABLE chat_messages (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    conversation_id UUID        NOT NULL REFERENCES conversations(id),
    sender_id       UUID        NOT NULL,
    sender_type     VARCHAR(20) NOT NULL,
    content         TEXT        NOT NULL,
    sent_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    read_at         TIMESTAMPTZ,
    CONSTRAINT chat_messages_pkey PRIMARY KEY (id),
    CONSTRAINT chat_messages_sender_type_check CHECK (sender_type IN ('CUSTOMER','AGENT','SYSTEM'))
);

CREATE INDEX idx_conversations_customer ON conversations (customer_id, updated_at DESC);
CREATE INDEX idx_conversations_status   ON conversations (status) WHERE status = 'OPEN';
CREATE INDEX idx_chat_messages_conv     ON chat_messages (conversation_id, sent_at ASC);
