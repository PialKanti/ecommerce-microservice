# order-service

The order-service handles checkout and the order lifecycle in the ecommerce platform. It converts a user's cart into an order, orchestrates the checkout SAGA over RabbitMQ, and exposes order management endpoints for both users and administrators. Security enforcement lives in the API Gateway; this service trusts the `X-User-Id` header injected by the gateway.

---

## Architecture Role

```
api-gateway  (validates JWT, enforces RBAC)
     │
     ▼  (lb://ORDER-SERVICE via Eureka)
order-service  (port 8085)
     │
     ├── PostgreSQL (order_db)            — orders, order_items, processed_events
     ├── Feign → lb://CART-SERVICE        — fetch cart on checkout
     └── RabbitMQ (ecommerce.topic)
           ├── publishes: order.created, order.confirmed, order.cancelled
           └── consumes:  inventory.reserved, inventory.reservation.failed,
                          cart.clear.failed, payment.initiated,
                          payment.succeeded, payment.failed
```

---

## Key Features

- **Dual controller pattern** — `OrderController` (`/api/v1/orders`) for authenticated users; `AdminOrderController` (`/api/v1/admin/orders`) for ADMIN/SUPPORT_AGENT.
- **Checkout** — `POST /orders/checkout` fetches the user's cart, snapshots items into an order, persists it with status `PENDING`, and publishes `OrderCreatedEvent`. Returns immediately; the SAGA advances status asynchronously.
- **Order state machine** — `PENDING` → `CONFIRMED` → `AWAITING_PAYMENT` → `PAID`, with cancellation available from `PENDING`, `CONFIRMED`, or `AWAITING_PAYMENT`.
- **SAGA orchestration** — responds to inventory and payment events to advance or cancel orders; publishes `OrderCancelledEvent` to trigger inventory release on failure paths.
- **Cancellation** — users can cancel their own orders; admins can cancel any order. Ownership validation runs before `OrderCancellationService` is invoked.
- **Idempotent event processing** — `processed_events` table ensures each incoming event is handled at most once even under retries or duplicate delivery.
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
| Spring Cloud OpenFeign | 2025.0.1 | REST client to cart-service |
| Netflix Eureka Client | 2025.0.1 | Service registration |
| MapStruct | 1.6.3 | Entity ↔ DTO mapping |
| Springdoc OpenAPI | 2.8.16 | Swagger UI |
| Java Virtual Threads | JDK 25 | Concurrency |

---

## API Endpoints

### User — Orders — `/api/v1/orders`

Requires a valid Bearer token. Operations are scoped to the authenticated user's own orders.

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/orders/checkout` | Convert the current cart into an order. Returns the order in `PENDING` status immediately; status advances asynchronously via the SAGA |
| GET | `/api/v1/orders/{id}` | Get an order owned by the current user |
| POST | `/api/v1/orders/{id}/cancel` | Cancel an order owned by the current user. Only `PENDING`, `CONFIRMED`, or `AWAITING_PAYMENT` orders can be cancelled |

**Checkout response:**
```json
{
  "success": true,
  "message": "Order placed successfully.",
  "data": {
    "orderId": 1,
    "orderNumber": "550e8400-e29b-41d4-a716-446655440000",
    "status": "PENDING"
  }
}
```

> Poll `GET /api/v1/orders/{id}` after checkout to observe the status transition to `CONFIRMED`, `AWAITING_PAYMENT`, or `CANCELLED`.

### Admin — Orders — `/api/v1/admin/orders`

Requires `ROLE_ADMIN` or `ROLE_SUPPORT_AGENT` with the matching permission (enforced by the API Gateway).

| Method | Path | Permission | Description |
|---|---|---|---|
| GET | `/api/v1/admin/orders/{id}` | `PERMISSION_ORDER_READ` | Get any order regardless of owner |
| POST | `/api/v1/admin/orders/{id}/cancel` | `PERMISSION_ORDER_CANCEL` | Cancel any cancellable order and release reserved inventory |

---

## Order Status Lifecycle

```
POST /checkout
    │
    ▼
 PENDING  ──(InventoryReservationFailedEvent)──► CANCELLED
    │
    ▼ InventoryReservedEvent
 CONFIRMED ──(CartClearFailedEvent)──────────► CANCELLED
    │
    ▼ PaymentInitiatedEvent
 AWAITING_PAYMENT ──(PaymentFailedEvent)────► CANCELLED
    │
    ▼ PaymentSucceededEvent
  PAID
```

Users and admins can trigger cancellation from `PENDING`, `CONFIRMED`, or `AWAITING_PAYMENT`.

---

## Messaging

### Published Events

| Routing Key | Trigger |
|---|---|
| `order.created` | Order persisted in `PENDING` state at checkout |
| `order.confirmed` | Inventory successfully reserved (`InventoryReservedEvent` received) |
| `order.cancelled` | Order cancelled — either manually or via SAGA compensation |

### Consumed Events

| Routing Key | Queue | Action |
|---|---|---|
| `inventory.reserved` | `order.inventory.reserved` | Set order to `CONFIRMED`, publish `OrderConfirmedEvent` |
| `inventory.reservation.failed` | `order.inventory.reservation.failed` | Cancel order, publish `OrderCancelledEvent` |
| `cart.clear.failed` | `order.cart.clear.failed` | Cancel order, publish `OrderCancelledEvent` |
| `payment.initiated` | `order.payment.initiated` | Set order to `AWAITING_PAYMENT`, store `paymentLink` |
| `payment.succeeded` | `order.payment.succeeded` | Set order to `PAID` |
| `payment.failed` | `order.payment.failed` | Cancel order, publish `OrderCancelledEvent` |

All events are exchanged on `ecommerce.topic`.

---

## Configuration

### `application.yml` overview

```yaml
spring:
  application:
    name: order-service
  threads:
    virtual:
      enabled: true
  datasource:
    url: ${ORDER_DB_URL:jdbc:postgresql://localhost:5432/order_db}
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
  port: 8085

eureka:
  client:
    service-url:
      default-zone: ${EUREKA_URL:http://localhost:8761/eureka/}
```

### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `ORDER_DB_URL` | `jdbc:postgresql://localhost:5432/order_db` | PostgreSQL JDBC URL |
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
- PostgreSQL 17 with a database named `order_db`
- RabbitMQ 3.13
- Eureka server running on port 8761
- cart-service running and registered with Eureka

### Start infrastructure with Docker

```bash
# From the project root — starts PostgreSQL, Redis, and RabbitMQ
docker compose up -d
```

PostgreSQL init scripts in `docker/postgres/init/` create the `order_db` database automatically.

### Run via Gradle

```bash
./gradlew :order-service:bootRun
```

### Swagger UI

```
http://localhost:8085/swagger-ui.html
```

---

## Database

- **Database:** `order_db`
- **Migrations:** Flyway, located at `src/main/resources/db/migration/`
- **JPA mode:** `validate` — Flyway owns the schema entirely

### Key Tables

| Table | Description |
|---|---|
| `orders` | Order header — `order_number` (UUID), `user_id`, `status`, `payment_link`, `cancelled_at`, `total_amount` |
| `order_items` | Line items snapshotted from the cart at checkout — `product_id`, `product_sku`, `product_name`, `unit_price`, `quantity` |
| `processed_events` | SAGA idempotency — stores processed `eventId` UUIDs to prevent duplicate event handling |

---

## Dependencies

| Module / Service | Relationship |
|---|---|
| `commons` | Compile-time — `ApiEndpoints`, `ApiResponse`, `BaseEntity`, `Auditable`, event DTOs |
| `eureka-server` | Runtime — service registry |
| `api-gateway` | Runtime — proxies all inbound traffic; enforces RBAC before forwarding |
| `cart-service` | Runtime — Feign client fetches cart contents on checkout |
| PostgreSQL | Runtime — primary data store |
| RabbitMQ | Runtime — SAGA event bus |

---

## Health Check

```
GET http://localhost:8085/actuator/health
```
