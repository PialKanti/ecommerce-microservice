# cart-service

The cart-service provides per-user shopping cart management for the ecommerce platform. It is purely self-service — all three endpoints are scoped to the authenticated caller and there is no admin surface. The service participates in the checkout SAGA over RabbitMQ by clearing the cart when an order is confirmed. Security enforcement lives in the API Gateway; this service trusts the `X-User-Id` header injected by the gateway.

---

## Architecture Role

```
api-gateway  (validates JWT, enforces RBAC)
     │
     ▼  (lb://CART-SERVICE via Eureka)
cart-service  (port 8084)
     │
     ├── PostgreSQL (cart_db)              — carts, cart_items, processed_events
     ├── Feign → lb://PRODUCT-SERVICE      — validates product exists and is active on addItem
     ├── Feign → lb://INVENTORY-SERVICE    — checks available quantity on addItem
     └── RabbitMQ (ecommerce.topic)
           ├── consumes: order.confirmed   → clear cart for the given userId
           └── publishes: cart.clear.failed
```

---

## Key Features

- **One cart per user** — `carts.user_id` is unique; adding items creates the cart automatically on first use.
- **Quantity merging** — adding a product already in the cart updates the existing line's quantity rather than creating a duplicate row. A `uk_cart_product` unique constraint on `(cart_id, product_id)` enforces this at the DB level.
- **Snapshot pricing** — `productSku`, `productName`, and `unitPrice` are snapshotted from the product response at add-time and never refreshed. Price or name changes after the item was added are not reflected in the cart.
- **Stock validation** — on `addItem`, the service calls inventory-service to ensure sufficient `availableQuantity` exists for the requested total before allowing the item.
- **Product validation** — on `addItem`, calls product-service to confirm the product exists and is active (409 if inactive).
- **SAGA participation** — `CartEventListener` clears the cart when `order.confirmed` is received and publishes `CartClearFailedEvent` if the clear throws an exception.
- **Idempotent operations** — `getCart` returns an empty response (not 404) if no cart exists; `clearCart` is a no-op if no cart exists.
- **Dead-letter queues** — failed messages after 3 retry attempts (exponential back-off 1 s → 10 s) land in `.dlq` queues on `ecommerce.dlx`.
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
| Spring AMQP / RabbitMQ | — | SAGA event messaging |
| Spring Cloud OpenFeign | 2025.0.1 | REST clients to product-service and inventory-service |
| Netflix Eureka Client | 2025.0.1 | Service registration |
| MapStruct | 1.6.3 | Entity ↔ DTO mapping |
| Springdoc OpenAPI | 2.8.16 | Swagger UI |
| Java Virtual Threads | JDK 25 | Concurrency |

---

## API Endpoints

### Cart — `/api/v1/cart`

All endpoints require a valid Bearer token. The API Gateway injects `X-User-Id` before the request reaches this service.

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/cart/items` | Add a product to the current user's cart. Validates product is active and inventory is sufficient. Merges quantity if the product is already in the cart |
| GET | `/api/v1/cart` | Retrieve the current user's cart. Returns an empty cart response (not 404) if no cart exists |
| DELETE | `/api/v1/cart` | Remove all items from the current user's cart. Idempotent — no-op if no cart exists |

**Add item request:**
```json
{
  "productId": 1,
  "quantity": 2
}
```

**Cart response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "items": [
      {
        "productId": 1,
        "productSku": "PHONE-001",
        "productName": "Smartphone Pro 128GB",
        "unitPrice": 499.99,
        "quantity": 2,
        "subtotal": 999.98
      }
    ],
    "totalQuantity": 2,
    "subtotal": 999.98
  }
}
```

---

## Messaging

### Consumed Events

| Routing Key | Queue | Action |
|---|---|---|
| `order.confirmed` | `cart.order.confirmed` | Clear the cart for the `userId` in the event; publish `CartClearFailedEvent` if the clear throws |

### Published Events

| Routing Key | Trigger |
|---|---|
| `cart.clear.failed` | Cart clear raised an exception after order was confirmed |

All events are published to the `ecommerce.topic` exchange.

---

## Configuration

### `application.yml` overview

```yaml
spring:
  application:
    name: cart-service
  threads:
    virtual:
      enabled: true
  datasource:
    url: ${CART_DB_URL:jdbc:postgresql://localhost:5432/cart_db}
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
  port: 8084

eureka:
  client:
    service-url:
      default-zone: ${EUREKA_URL:http://localhost:8761/eureka/}
```

### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `CART_DB_URL` | `jdbc:postgresql://localhost:5432/cart_db` | PostgreSQL JDBC URL |
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
- PostgreSQL 17 with a database named `cart_db`
- RabbitMQ 3.13
- Eureka server running on port 8761
- product-service and inventory-service running and registered with Eureka

### Start infrastructure with Docker

```bash
# From the project root — starts PostgreSQL, Redis, and RabbitMQ
docker compose up -d
```

PostgreSQL init scripts in `docker/postgres/init/` create the `cart_db` database automatically.

### Run via Gradle

```bash
./gradlew :cart-service:bootRun
```

### Swagger UI

```
http://localhost:8084/swagger-ui.html
```

---

## Database

- **Database:** `cart_db`
- **Migrations:** Flyway, located at `src/main/resources/db/migration/`
- **JPA mode:** `validate` — Flyway owns the schema entirely

### Key Tables

| Table | Description |
|---|---|
| `carts` | One row per user (`user_id` unique) |
| `cart_items` | Line items per cart; `(cart_id, product_id)` unique; stores snapshotted `product_sku`, `product_name`, `unit_price` |
| `processed_events` | SAGA idempotency — stores processed `eventId` UUIDs to prevent duplicate event handling |

---

## Dependencies

| Module / Service | Relationship |
|---|---|
| `commons` | Compile-time — `ApiEndpoints`, `ApiResponse`, `BaseEntity`, event DTOs |
| `eureka-server` | Runtime — service registry |
| `api-gateway` | Runtime — proxies all inbound traffic; enforces authentication before forwarding |
| `product-service` | Runtime — Feign client validates product exists and is active on `addItem` |
| `inventory-service` | Runtime — Feign client checks available stock on `addItem` |
| PostgreSQL | Runtime — primary data store |
| RabbitMQ | Runtime — SAGA event bus |

---

## Health Check

```
GET http://localhost:8084/actuator/health
```
