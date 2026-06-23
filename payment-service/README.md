# payment-service

The payment-service handles payment processing for the ecommerce platform using Stripe Checkout. It creates hosted payment sessions, tracks payment lifecycle, expires stale sessions on a scheduler, and participates in the checkout SAGA over RabbitMQ. Security enforcement lives in the API Gateway; this service trusts the `X-User-Id` header injected by the gateway.

---

## Architecture Role

```
api-gateway  (validates JWT, enforces RBAC)
     │
     ▼  (lb://PAYMENT-SERVICE via Eureka)
payment-service  (port 8086)
     │
     ├── PostgreSQL (payment_db)       — payments, processed_events
     ├── Stripe Checkout API           — hosted payment sessions
     └── RabbitMQ (ecommerce.topic)
           ├── consumes: order.confirmed  → create Stripe session
           └── publishes: payment.initiated, payment.succeeded, payment.failed
```

Stripe redirects the user's browser back to the API Gateway webhook endpoints (`/api/v1/payments/webhook/success|cancel`) after the payment completes or is cancelled.

---

## Key Features

- **Stripe Checkout integration** — creates hosted payment sessions with per-item line items, order metadata, and configurable currency. Sessions expire after 30 minutes (configurable).
- **Idempotent session creation** — if an active `INITIATED` payment for the same order already exists and has not expired, the existing session is returned rather than creating a new one.
- **Automatic expiration** — `PaymentExpirationScheduler` runs on a fixed delay (default 60 s) to find and expire overdue `INITIATED` payments: expires the Stripe session, marks the payment `CANCELLED`, and publishes `PaymentFailedEvent` to trigger order cancellation.
- **SAGA participation** — `PaymentEventListener` listens for `order.confirmed` to create a payment session and publishes `PaymentInitiatedEvent`, `PaymentSucceededEvent`, or `PaymentFailedEvent`.
- **Dual controller pattern** — `PaymentController` (`/api/v1/payments`) is called internally by the SAGA; `AdminPaymentController` (`/api/v1/admin/payments`) provides lookup for ADMIN/SUPPORT_AGENT. `PaymentWebhookController` (`/api/v1/payments/webhook`) handles Stripe browser redirects without authentication.
- **Terminal state protection** — once a payment reaches `SUCCEEDED`, `FAILED`, or `CANCELLED` it cannot be overwritten.
- **Idempotent event processing** — `processed_events` table ensures each incoming event is handled at most once.
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
| Stripe Java SDK | — | Checkout session creation and expiration |
| Netflix Eureka Client | 2025.0.1 | Service registration |
| MapStruct | 1.6.3 | Entity ↔ DTO mapping |
| Springdoc OpenAPI | 2.8.16 | Swagger UI |
| Java Virtual Threads | JDK 25 | Concurrency |

---

## API Endpoints

### Webhooks — `/api/v1/payments/webhook`

No authentication required — Stripe redirects the browser here after payment.

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/payments/webhook/success` | Handle successful Stripe payment redirect (`session_id` query param). Marks payment `SUCCEEDED` and publishes `PaymentSucceededEvent` |
| GET | `/api/v1/payments/webhook/cancel` | Handle cancelled Stripe payment redirect (`session_id` query param). Marks payment `FAILED` and publishes `PaymentFailedEvent` |

### Internal — Payment Session — `/api/v1/payments`

Called internally by order-service during the SAGA. Requires a valid Bearer token (enforced by the API Gateway's `anyRequest().authenticated()` catch-all).

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/payments` | Create a Stripe Checkout session for an order. Returns the existing session if one is still active and unexpired |

### Admin — Payments — `/api/v1/admin/payments`

Requires `ROLE_ADMIN` or `ROLE_SUPPORT_AGENT` with the matching permission (enforced by the API Gateway).

| Method | Path | Permission | Description |
|---|---|---|---|
| GET | `/api/v1/admin/payments/{id}` | `PERMISSION_PAYMENT_READ` | Get payment by internal ID |
| GET | `/api/v1/admin/payments/orders/{orderId}` | `PERMISSION_PAYMENT_READ` | Get the latest payment for a given order |

---

## Payment Status Lifecycle

```
INITIATED ──(webhook success / PaymentSucceededEvent)──► SUCCEEDED
          ──(webhook cancel / PaymentFailedEvent)──────► FAILED
          ──(scheduler expiration)────────────────────► CANCELLED
```

Terminal states (`SUCCEEDED`, `FAILED`, `CANCELLED`) cannot be overwritten.

---

## Messaging

### Published Events

| Routing Key | Trigger |
|---|---|
| `payment.initiated` | Stripe Checkout session created successfully |
| `payment.succeeded` | Stripe success webhook received |
| `payment.failed` | Stripe cancel webhook received, or payment expired by scheduler |

### Consumed Events

| Routing Key | Queue | Action |
|---|---|---|
| `order.confirmed` | `payment.order.confirmed` | Create Stripe Checkout session; publish `PaymentInitiatedEvent` |

All events are exchanged on `ecommerce.topic`.

---

## Configuration

### `application.yml` overview

```yaml
spring:
  application:
    name: payment-service
  threads:
    virtual:
      enabled: true
  datasource:
    url: ${PAYMENT_DB_URL:jdbc:postgresql://localhost:5432/payment_db}
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

stripe:
  api-key: ${STRIPE_API_KEY:...}
  success-url: ${STRIPE_SUCCESS_URL:http://localhost:8080/api/v1/payments/webhook/success?session_id={CHECKOUT_SESSION_ID}}
  cancel-url: ${STRIPE_CANCEL_URL:http://localhost:8080/api/v1/payments/webhook/cancel?session_id={CHECKOUT_SESSION_ID}}
  currency: ${STRIPE_CURRENCY:bdt}

payment:
  expiration:
    lifetime: ${PAYMENT_EXPIRATION_LIFETIME:PT30M}
    check-delay: ${PAYMENT_EXPIRATION_CHECK_DELAY:PT60S}

server:
  port: 8086

eureka:
  client:
    service-url:
      default-zone: ${EUREKA_URL:http://localhost:8761/eureka/}
```

### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `PAYMENT_DB_URL` | `jdbc:postgresql://localhost:5432/payment_db` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `admin` | PostgreSQL username |
| `DB_PASSWORD` | `admin@123` | PostgreSQL password |
| `STRIPE_API_KEY` | (test key in yml — dev only) | Stripe secret API key |
| `STRIPE_SUCCESS_URL` | `http://localhost:8080/api/v1/payments/webhook/success?session_id={CHECKOUT_SESSION_ID}` | Stripe success redirect URL |
| `STRIPE_CANCEL_URL` | `http://localhost:8080/api/v1/payments/webhook/cancel?session_id={CHECKOUT_SESSION_ID}` | Stripe cancel redirect URL |
| `STRIPE_CURRENCY` | `bdt` | Currency code passed to Stripe (ISO 4217) |
| `PAYMENT_EXPIRATION_LIFETIME` | `PT30M` | ISO-8601 duration — how long a payment session remains active before expiration |
| `PAYMENT_EXPIRATION_CHECK_DELAY` | `PT60S` | ISO-8601 duration — how often the expiration scheduler runs |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ hostname |
| `RABBITMQ_PORT` | `5672` | RabbitMQ AMQP port |
| `RABBITMQ_USERNAME` | `admin` | RabbitMQ username |
| `RABBITMQ_PASSWORD` | `admin@123` | RabbitMQ password |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | Eureka registry URL |

> Replace the default `STRIPE_API_KEY` with your own Stripe test or live key in production. The default in `application.yml` is a dev-only test key and must not be used in any non-local environment.

---

## Local Setup

### Prerequisites

- JDK 25
- PostgreSQL 17 with a database named `payment_db`
- RabbitMQ 3.13
- Eureka server running on port 8761
- A Stripe account with a test API key

### Start infrastructure with Docker

```bash
# From the project root — starts PostgreSQL, Redis, and RabbitMQ
docker compose up -d
```

PostgreSQL init scripts in `docker/postgres/init/` create the `payment_db` database automatically.

### Run via Gradle

```bash
./gradlew :payment-service:bootRun
```

### Swagger UI

```
http://localhost:8086/swagger-ui.html
```

---

## Database

- **Database:** `payment_db`
- **Migrations:** Flyway, located at `src/main/resources/db/migration/`
- **JPA mode:** `validate` — Flyway owns the schema entirely

### Key Tables

| Table | Description |
|---|---|
| `payments` | Payment record per order — `order_id`, `stripe_session_id`, `payment_link`, `status` (`INITIATED`/`SUCCEEDED`/`FAILED`/`CANCELLED`), `expires_at` |
| `processed_events` | SAGA idempotency — stores processed `eventId` UUIDs to prevent duplicate event handling |

---

## Dependencies

| Module / Service | Relationship |
|---|---|
| `commons` | Compile-time — `ApiEndpoints`, `ApiResponse`, `BaseEntity`, event DTOs |
| `eureka-server` | Runtime — service registry |
| `api-gateway` | Runtime — proxies all inbound traffic; enforces RBAC and authentication before forwarding |
| Stripe API | External — hosted checkout session creation and expiration |
| PostgreSQL | Runtime — primary data store |
| RabbitMQ | Runtime — SAGA event bus |

---

## Health Check

```
GET http://localhost:8086/actuator/health
```
