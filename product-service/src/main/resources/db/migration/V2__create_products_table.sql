CREATE TABLE products
(
    id          BIGSERIAL        PRIMARY KEY,
    sku         VARCHAR(100)     NOT NULL UNIQUE,
    name        VARCHAR(200)     NOT NULL,
    description VARCHAR(1000),
    price       DOUBLE PRECISION NOT NULL,
    is_active   BOOLEAN          NOT NULL DEFAULT TRUE,
    category_id BIGINT           NOT NULL,
    image_url   VARCHAR(250),
    created_at  TIMESTAMP        NOT NULL,
    modified_at TIMESTAMP        NOT NULL,
    created_by  BIGINT,
    modified_by BIGINT,
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
            REFERENCES categories (id)
);

CREATE INDEX idx_products_category_id ON products (category_id);
