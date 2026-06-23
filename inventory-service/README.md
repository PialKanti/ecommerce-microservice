# inventory-service

The inventory-service manages stock levels for products in the ecommerce platform. It tracks total quantity, reserved quantity, and derived available quantity per product, and participates in the checkout SAGA over RabbitMQ to reserve and release stock automatically. Security enforcement lives in the API Gateway; this service trusts the `X-User-Id` header injected by the gateway.

---

## Architecture Role

```
api-gateway  (validates JWT, enforces RBAC)
     │
     ▼  (lb://INVENTORY-SERVICE via Eureka)
inventory-service  (port 8083)
     │
     ├── PostgreSQL (inventory_db)       — inventories, processed_events
     ├── Feign → lb://PRODUCT-SERVICE   — validates productId on inventory creation
     └── RabbitMQ (ecommerce.topic)
           ├── consumes: order.created   → reserve stock
           └── consumes: order.cancelled → release stock
           ├── publishes: inventory.reserved
           └── publishes: inventory.reservation.failed
```

---

## Key Features

- **Dual controller pattern** — `InventoryController` (`/api/v1/inventory`) is public read-only; `AdminInventoryController` (`/api/v1/admin/inventory`) is write-protected.
- **Stock operations** — `increase`, `decrease`, `reserve`, and `release` endpoints on `POST /admin/inventory/{productId}/{operation}`.
- **Optimistic locking** — the `Inventory` entity uses a `version` column to prevent concurrent stock corruption.
- **DB-level constraints** — `CHECK` constraints enforce `totalQuantity >= 0`, `reservedQuantity >= 0`, and `reservedQuantity <= totalQuantity`.
- **Product validation** — on create, calls `ProductServiceClient` to confirm the product exists before creating an inventory record.
- **SAGA participation** — `InventoryEventListener` listens for `order.created` events to reserve stock and `order.cancelled` events to release it, with idempotency enforced via the `processed_events` table.
- **Dead-letter queues** — failed messages after 3 retry attempts (exponential back-off 1 s → 10 s) land in `.dlq` queues on `ecommerce.dlx`.
- **Flyway migrations** — schema is version-controlled under `src/main/resources/db/migration`.
- **OpenAPI / Swagger UI** — full API documentation available at `/swagger-ui.html`.
- **Virtual threads** — enabled via `spring.threads.virtual.enabled: true`.

---

## Technologies

| Technology | Version | Purpose |
|---|---|---|
| Spring Boot | 3.5.14 | Application framework |
| Spring Data JPA + Hibernate | — | ORM, optimistic locking |
| Flyway | — | Schema migrations |
| PostgreSQL | 17.5 | Primary data store |
| Spring AMQP / RabbitMQ | — | SAGA event messaging |
| Spring Cloud OpenFeign | 2025.0.1 | REST client to product-service |
| Netflix Eureka Client | 2025.0.1 | Service registration |
| MapStruct | 1.6.3 | Entity ↔ DTO mapping |
| Springdoc OpenAPI | 2.8.16 | Swagger UI |
| Java Virtual Threads | JDK 25 | Concurrency |

---

## API Endpoints

### Public — Inventory — `/api/v1/inventory`

No authentication required.

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/inventory/{productId}` | Get inventory levels (`totalQuantity`, `reservedQuantity`, `availableQuantity`) for a product |

### Admin — Inventory — `/api/v1/admin/inventory`

Requires `ROLE_ADMIN` or `ROLE_INVENTORY_MANAGER` with the matching permission (enforced by the API Gateway).

| Method | Path | Permission | Description |
|---|---|---|---|
| POST | `/api/v1/admin/inventory` | `PERMISSION_INVENTORY_MANAGE` | Create an inventory record for a product (validates product exists) |
| GET | `/api/v1/admin/inventory/{productId}` | `PERMISSION_INVENTORY_READ` | Get inventory by product ID |
| POST | `/api/v1/admin/inventory/{productId}/increase` | `PERMISSION_INVENTORY_MANAGE` | Increase total stock quantity |
| POST | `/api/v1/admin/inventory/{productId}/decrease` | `PERMISSION_INVENTORY_MANAGE` | Decrease total stock quantity (fails if it would go below reserved) |
| POST | `/api/v1/admin/inventory/{productId}/reserve` | `PERMISSION_INVENTORY_MANAGE` | Reserve available stock (fails if insufficient available quantity) |
| POST | `/api/v1/admin/inventory/{productId}/release` | `PERMISSION_INVENTORY_MANAGE` | Release reserved stock (fails if releasing more than currently reserved) |

---

## Messaging

### Consumed Events

| Routing Key | Queue | Action |
|---|---|---|
| `order.created` | `inventory.order.created` | Reserve stock for each order item; publish `InventoryReservedEvent` or `InventoryReservationFailedEvent` |
| `order.cancelled` | `inventory.order.cancelled` | Release reserved stock for each order item |

### Published Events

| Routing Key | Trigger |
|---|---|
| `inventory.reserved` | All items reserved successfully |
| `inventory.reservation.failed` | Insufficient stock for one or more items |

All events are published to the `ecommerce.topic` exchange.

---

## Configuration

### `application.yml` overview

```yaml
spring:
  application:
    name: inventory-service
  threads:
    virtual:
      enabled: true
  datasource:
    url: ${INVENTORY_DB_URL:jdbc:postgresql://localhost:5432/inventory_db}
    username: ${DB_USERNAME:admin}
    password: ${DB_PASSWORD:admin@123}
  jpa:
    hibernate:
      ddl-auto: validate      # Flyway owns the schema; JPA only validates
  flyway:
    enabled: true
    locations: classpath:db/migration
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:admin}
    password: ${RABBITMQ_PASSWORD:admin@123}
    listener:
      direct:
        retry:
          enabled: true
          initial-interval: 1000ms
          max-attempts: 3
          multiplier: 2.0
          max-interval: 10000ms

server:
  port: 8083

eureka:
  client:
    service-url:
      default-zone: ${EUREKA_URL:http://localhost:8761/eureka/}
```

### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `INVENTORY_DB_URL` | `jdbc:postgresql://localhost:5432/inventory_db` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `admin` | PostgreSQL username |
| `DB_PASSWORD` | `admin@123` | PostgreSQL password |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ hostname |
| `RABBITMQ_PORT` | `5672` | RabbitMQ AMQP port |
| `RABBITMQ_USERNAME` | `admin` | RabbitMQ username |
| `RABBITMQ_PASSWORD` | `admin@123` | RabbitMQ password |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | Eureka registry URL |

---

## Local Setup

### Prerequisites

- JDK 25
- PostgreSQL 17 with a database named `inventory_db`
- RabbitMQ 3.13
- Eureka server running on port 8761
- product-service running and registered with Eureka

### Start infrastructure with Docker

```bash
# From the project root — starts PostgreSQL, Redis, and RabbitMQ
docker compose up -d
```

PostgreSQL init scripts in `docker/postgres/init/` create the `inventory_db` database automatically.

### Run via Gradle

```bash
./gradlew :inventory-service:bootRun
```

### Swagger UI

```
http://localhost:8083/swagger-ui.html
```

---

## Database

- **Database:** `inventory_db`
- **Migrations:** Flyway, located at `src/main/resources/db/migration/`
- **JPA mode:** `validate` — Flyway owns the schema entirely

### Key Tables

| Table | Description |
|---|---|
| `inventories` | Per-product stock record with `product_id` (unique), `total_quantity`, `reserved_quantity`, `version` (optimistic lock) |
| `processed_events` | SAGA idempotency — stores processed `eventId` UUIDs to prevent duplicate event handling |

---

## Dependencies

| Module / Service | Relationship |
|---|---|
| `commons` | Compile-time — `ApiEndpoints`, `ApiResponse`, `BaseEntity`, `Auditable`, event DTOs |
| `eureka-server` | Runtime — service registry |
| `api-gateway` | Runtime — proxies all inbound traffic; enforces RBAC before forwarding |
| `product-service` | Runtime — Feign client validates `productId` on inventory creation |
| PostgreSQL | Runtime — primary data store |
| RabbitMQ | Runtime — SAGA event bus |

---

## Health Check

```
GET http://localhost:8083/actuator/health
```
