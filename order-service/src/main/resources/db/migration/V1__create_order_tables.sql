CREATE TABLE orders (
    id           BIGSERIAL PRIMARY KEY,
    order_number UUID             NOT NULL,
    user_id      BIGINT           NOT NULL,
    status       VARCHAR(30)      NOT NULL,
    total_amount DOUBLE PRECISION NOT NULL,
    cancelled_at TIMESTAMP,
    created_at   TIMESTAMP        NOT NULL,
    modified_at  TIMESTAMP        NOT NULL,
    created_by   BIGINT,
    modified_by  BIGINT,
    CONSTRAINT uk_orders_order_number
        UNIQUE (order_number),
    CONSTRAINT chk_orders_status
        CHECK (status IN ('CONFIRMED', 'PAID', 'CANCELLED')),
    CONSTRAINT chk_orders_total_amount_non_negative
        CHECK (total_amount >= 0),
    CONSTRAINT chk_orders_cancelled_at_required_when_cancelled
        CHECK (
            (status = 'CANCELLED' AND cancelled_at IS NOT NULL)
            OR
            (status <> 'CANCELLED' AND cancelled_at IS NULL)
        )
);

CREATE INDEX idx_orders_user_id     ON orders (user_id);
CREATE INDEX idx_orders_status      ON orders (status);
CREATE INDEX idx_orders_order_number ON orders (order_number);

CREATE TABLE order_items (
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT           NOT NULL,
    product_id   BIGINT           NOT NULL,
    product_name VARCHAR(200)     NOT NULL,
    unit_price   DOUBLE PRECISION NOT NULL,
    quantity     INTEGER          NOT NULL,
    total_price  DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT chk_order_items_quantity_positive
        CHECK (quantity > 0),
    CONSTRAINT chk_order_items_unit_price_positive
        CHECK (unit_price > 0),
    CONSTRAINT chk_order_items_total_price_positive
        CHECK (total_price > 0)
);

CREATE INDEX idx_order_items_order_id   ON order_items (order_id);
CREATE INDEX idx_order_items_product_id ON order_items (product_id);
