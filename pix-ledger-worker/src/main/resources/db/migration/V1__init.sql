CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    account VARCHAR(255) NOT NULL,
    direction VARCHAR(16) NOT NULL,
    amount_cents BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    -- Guards idempotent posting: a redelivered Kafka message tries to insert the same
    -- (transaction_id, direction) pair again and collides here instead of double-posting.
    CONSTRAINT uq_ledger_entries_transaction_direction UNIQUE (transaction_id, direction)
);

CREATE INDEX idx_ledger_entries_transaction_id ON ledger_entries (transaction_id);
