ALTER TABLE orders DROP CONSTRAINT chk_orders_status;
ALTER TABLE orders
    ADD CONSTRAINT chk_orders_status
        CHECK (status IN ('PENDING', 'CONFIRMED', 'PAID', 'CANCELLED'));

CREATE TABLE processed_events (
    event_id     UUID      NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_processed_events PRIMARY KEY (event_id)
);
