CREATE TABLE categories
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    code        VARCHAR(50)  NOT NULL UNIQUE,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL,
    modified_at TIMESTAMP    NOT NULL,
    created_by  BIGINT,
    modified_by BIGINT
);
