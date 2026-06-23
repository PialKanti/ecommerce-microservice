CREATE TABLE payments (
    id           BIGSERIAL     PRIMARY KEY,
    order_id     BIGINT        NOT NULL,
    order_number UUID          NOT NULL,
    user_id      BIGINT        NOT NULL,
    session_id   VARCHAR(255)  NOT NULL,
    payment_link VARCHAR(2048) NOT NULL,
    status       VARCHAR(30)   NOT NULL,
    expires_at   TIMESTAMP     NOT NULL,
    created_at   TIMESTAMP     NOT NULL,
    modified_at  TIMESTAMP     NOT NULL,
    created_by   BIGINT,
    modified_by  BIGINT,
    CONSTRAINT uk_payments_session_id UNIQUE (session_id),
    CONSTRAINT chk_payments_status
        CHECK (status IN ('INITIATED', 'SUCCEEDED', 'FAILED', 'CANCELLED'))
);

CREATE INDEX idx_payments_order_id   ON payments (order_id);
CREATE INDEX idx_payments_status     ON payments (status);
CREATE INDEX idx_payments_initiated_expires
    ON payments (expires_at) WHERE status = 'INITIATED';

CREATE TABLE processed_events (
    event_id     UUID      NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_processed_events PRIMARY KEY (event_id)
);
