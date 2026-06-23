CREATE TABLE carts
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT    NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    created_by  BIGINT,
    modified_by BIGINT
);

CREATE UNIQUE INDEX uk_carts_user_id ON carts (user_id);

CREATE TABLE cart_items
(
    id           BIGSERIAL PRIMARY KEY,
    cart_id      BIGINT           NOT NULL,
    product_id   BIGINT           NOT NULL,
    product_sku  VARCHAR(255)     NOT NULL,
    product_name VARCHAR(255)     NOT NULL,
    quantity     INTEGER          NOT NULL,
    unit_price   DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id)
        REFERENCES carts (id),
    CONSTRAINT uk_cart_product
        UNIQUE (cart_id, product_id),
    CONSTRAINT chk_cart_items_quantity_positive
        CHECK (quantity > 0),
    CONSTRAINT chk_cart_items_unit_price_positive
        CHECK (unit_price > 0)
);

CREATE INDEX idx_cart_items_cart_id ON cart_items (cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items (product_id);
