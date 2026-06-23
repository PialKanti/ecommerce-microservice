# product-service

The product-service manages the ecommerce product catalog — categories and products. It exposes public read-only endpoints accessible without authentication and admin write endpoints protected by the API Gateway's RBAC. Security enforcement lives entirely in the gateway; this service is a plain Spring MVC application that trusts the `X-User-Id` header injected by the gateway.

---

## Architecture Role

```
api-gateway  (validates JWT, enforces RBAC)
     │
     ▼  (lb://PRODUCT-SERVICE via Eureka)
product-service  (port 8082)
     │
     └── PostgreSQL (product_db)   — categories, products
```

Product data is also consumed upstream by inventory-service and cart-service via Feign clients targeting `lb://PRODUCT-SERVICE`.

---

## Key Features

- **Dual controller pattern** — every resource has a public read-only controller (`/api/v1/products`, `/api/v1/categories`) and an admin write controller (`/api/v1/admin/products`, `/api/v1/admin/categories`). Admin endpoints also return inactive records hidden from public endpoints.
- **Product catalog** — full CRUD for products with SKU, name, description, price, image URL, and category assignment. `sku` is immutable after creation.
- **Category management** — hierarchical product grouping via category code (unique and immutable) and name.
- **Soft delete** — products and categories use `isActive` toggling instead of row deletion. The status endpoint (`PUT /{id}/status`) is the only way to deactivate a record.
- **Flexible filtering** — paginated list endpoints accept optional `search` (partial match on name and SKU/code), `categoryId`, price range, and `isActive` filter via a single JPQL query. Null parameters are skipped.
- **Auditing** — `createdAt`, `modifiedAt` (auto-set by JPA lifecycle hooks), `createdBy`, and `modifiedBy` (set from `X-User-Id` header) are recorded on every entity.
- **Flyway migrations** — schema is version-controlled under `src/main/resources/db/migration`.
- **OpenAPI / Swagger UI** — full API documentation available at `/swagger-ui.html`.
- **Virtual threads** — enabled via `spring.threads.virtual.enabled: true`.

---

## Technologies

| Technology | Version | Purpose |
|---|---|---|
| Spring Boot | 3.5.14 | Application framework |
| Spring Data JPA + Hibernate | — | ORM, PostgreSQL dialect |
| Flyway | — | Schema migrations |
| PostgreSQL | 17.5 | Primary data store |
| Netflix Eureka Client | 2025.0.1 | Service registration |
| MapStruct | 1.6.3 | Entity ↔ DTO mapping |
| Springdoc OpenAPI | 2.8.16 | Swagger UI |
| Java Virtual Threads | JDK 25 | Concurrency |

---

## API Endpoints

### Public — Products — `/api/v1/products`

No authentication required. Only active products are returned.

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/products` | Paginated list of active products. Supports `search`, `categoryId`, `minPrice`, `maxPrice`, `page`, `size` |
| GET | `/api/v1/products/{id}` | Get a single active product by ID |

### Public — Categories — `/api/v1/categories`

No authentication required. Only active categories are returned.

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/categories` | Paginated list of active categories. Supports `search`, `page`, `size` |
| GET | `/api/v1/categories/{id}` | Get a single active category by ID |

### Admin — Products — `/api/v1/admin/products`

Requires `ROLE_ADMIN` or `ROLE_PRODUCT_MANAGER` with the matching permission (enforced by the API Gateway).

| Method | Path | Permission | Description |
|---|---|---|---|
| GET | `/api/v1/admin/products` | `PERMISSION_PRODUCT_READ` | Paginated list including inactive. Supports `search`, `categoryId`, `minPrice`, `maxPrice`, `isActive`, `page`, `size` |
| GET | `/api/v1/admin/products/{id}` | `PERMISSION_PRODUCT_READ` | Get any product by ID regardless of active status |
| POST | `/api/v1/admin/products` | `PERMISSION_PRODUCT_CREATE` | Create a new product |
| PUT | `/api/v1/admin/products/{id}` | `PERMISSION_PRODUCT_UPDATE` | Update product details (`sku` is immutable) |
| PUT | `/api/v1/admin/products/{id}/status` | `PERMISSION_PRODUCT_UPDATE` | Activate or deactivate a product (`isActive` query param) |

### Admin — Categories — `/api/v1/admin/categories`

Requires `ROLE_ADMIN` or `ROLE_PRODUCT_MANAGER` with the matching permission.

| Method | Path | Permission | Description |
|---|---|---|---|
| GET | `/api/v1/admin/categories` | `PERMISSION_CATEGORY_READ` | Paginated list including inactive. Supports `search`, `isActive`, `page`, `size` |
| GET | `/api/v1/admin/categories/{id}` | `PERMISSION_CATEGORY_READ` | Get any category by ID regardless of active status |
| POST | `/api/v1/admin/categories` | `PERMISSION_CATEGORY_CREATE` | Create a new category (`code` is unique and immutable) |
| PUT | `/api/v1/admin/categories/{id}` | `PERMISSION_CATEGORY_UPDATE` | Update category name and description only |
| PUT | `/api/v1/admin/categories/{id}/status` | `PERMISSION_CATEGORY_UPDATE` | Activate or deactivate a category (`isActive` query param) |

---

## Configuration

### `application.yml` overview

```yaml
spring:
  application:
    name: product-service
  threads:
    virtual:
      enabled: true
  datasource:
    url: ${PRODUCT_DB_URL:jdbc:postgresql://localhost:5432/product_db}
    username: ${DB_USERNAME:admin}
    password: ${DB_PASSWORD:admin@123}
  jpa:
    hibernate:
      ddl-auto: validate      # Flyway owns the schema; JPA only validates
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8082

eureka:
  client:
    service-url:
      default-zone: ${EUREKA_URL:http://localhost:8761/eureka/}
```

### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `PRODUCT_DB_URL` | `jdbc:postgresql://localhost:5432/product_db` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `admin` | PostgreSQL username |
| `DB_PASSWORD` | `admin@123` | PostgreSQL password |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | Eureka registry URL |

---

## Local Setup

### Prerequisites

- JDK 25
- PostgreSQL 17 with a database named `product_db`
- Eureka server running on port 8761

### Start infrastructure with Docker

```bash
# From the project root — starts PostgreSQL, Redis, and RabbitMQ
docker compose up -d
```

PostgreSQL init scripts in `docker/postgres/init/` create the `product_db` database automatically.

### Run via Gradle

```bash
./gradlew :product-service:bootRun
```

### Swagger UI

```
http://localhost:8082/swagger-ui.html
```

---

## Database

- **Database:** `product_db`
- **Migrations:** Flyway, located at `src/main/resources/db/migration/`
- **JPA mode:** `validate` — Flyway owns the schema entirely

### Key Tables

| Table | Description |
|---|---|
| `categories` | Product categories with `code` (unique), `name`, `description`, `is_active` |
| `products` | Product catalog with `sku` (unique), `name`, `description`, `price`, `image_url`, `is_active`, and FK to `categories` |

---

## Dependencies

| Module / Service | Relationship |
|---|---|
| `commons` | Compile-time — `ApiEndpoints`, `ApiResponse`, `PaginatedResponse`, `BaseEntity`, `Auditable` |
| `eureka-server` | Runtime — service registry |
| `api-gateway` | Runtime — proxies all inbound traffic; enforces RBAC before forwarding |
| PostgreSQL | Runtime — primary data store |

---

## Health Check

```
GET http://localhost:8082/actuator/health
```
