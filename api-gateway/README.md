# api-gateway

The API Gateway is the single entry point for all client traffic in the ecommerce microservices system. It handles JWT authentication, role/permission-based access control (RBAC), token blacklist enforcement, and forwards authenticated user context to downstream services via internal HTTP headers.

---

## Architecture Role

```
Client
  │
  ▼
api-gateway  (port 8080)
  │   ├── JWT validation + RBAC (Spring Security)
  │   ├── Redis blacklist + user-block check
  │   └── Injects X-User-Id / X-Username / X-User-Roles / X-User-Permissions headers
  │
  ├──► auth-service  (via Eureka lb://AUTH-SERVICE)
  └──► (future services via Eureka)
       │
  eureka-server  (service registry — port 8761)
```

The gateway uses **Spring Cloud Gateway Server WebMVC** (servlet-based, not reactive), backed by virtual threads for high throughput without the reactive programming model.

---

## Key Features

- **Stateless JWT authentication** — verifies the `Authorization: Bearer <token>` header on every request using the shared signing key.
- **Redis-backed token blacklist** — tokens are SHA-256 hashed and checked against the `auth:blacklist:{hash}` key to invalidate logged-out tokens immediately.
- **Redis-backed user block check** — checks `auth:user:blocked:{userId}` before allowing access, so admin-disabled accounts are rejected within milliseconds.
- **Fine-grained RBAC** — admin routes require both a `RoleCode` (e.g., `ADMIN`) and a specific `PermissionCode` (e.g., `PERMISSION_ROLE_CREATE`) to be present in the token's claims.
- **User context propagation** — after authentication, a `UserContextPropagationFilter` injects four headers so downstream services receive the caller's identity without re-validating the JWT:

| Header | Value |
|---|---|
| `X-User-Id` | Numeric user ID |
| `X-Username` | Username string |
| `X-User-Roles` | Comma-separated roles (e.g., `ROLE_ADMIN`) |
| `X-User-Permissions` | Comma-separated permission codes |

- **Eureka client** — discovers upstream services by name; no hardcoded URLs required.

---

## Technologies

| Technology | Version | Purpose |
|---|---|---|
| Spring Boot | 3.5.14 | Application framework |
| Spring Cloud Gateway (WebMVC) | 2025.0.1 | Servlet-based routing |
| Spring Security | 6.x | Filter chain, RBAC |
| Spring Data Redis | — | Blacklist / user-block checks |
| Netflix Eureka Client | 2025.0.1 | Service discovery |
| JJWT | 0.12.6 | JWT parsing and validation |
| Spring Boot Actuator | — | `/actuator/health`, `/actuator/info` |
| Java Virtual Threads | JDK 25 | High-concurrency I/O |

---

## Route Configuration

All routes load-balance against the Eureka-registered service name `AUTH-SERVICE`.

| Route ID | Path Pattern | Destination |
|---|---|---|
| `auth-service-public` | `/api/v1/auth/**` | auth-service |
| `auth-service-user-profile` | `/api/v1/users/**` | auth-service |
| `auth-service-admin-roles` | `/api/v1/admin/roles/**` | auth-service |
| `auth-service-admin-permissions` | `/api/v1/admin/permissions/**` | auth-service |
| `auth-service-admin-users` | `/api/v1/admin/users/**` | auth-service |

---

## Access Control Rules

| Path | Method | Rule |
|---|---|---|
| `/api/v1/auth/login` | POST | Public |
| `/api/v1/auth/register` | POST | Public |
| `/api/v1/auth/refresh` | POST | Public |
| `/actuator/health` | GET | Public |
| `/v3/api-docs/**`, `/swagger-ui/**` | GET | Public |
| `/api/v1/admin/roles/**` | GET | `ROLE_ADMIN` + `PERMISSION_ROLE_READ` |
| `/api/v1/admin/roles` | POST | `ROLE_ADMIN` + `PERMISSION_ROLE_CREATE` |
| `/api/v1/admin/roles/**` | PUT | `ROLE_ADMIN` + `PERMISSION_ROLE_UPDATE` |
| `/api/v1/admin/roles/**` | DELETE | `ROLE_ADMIN` + `PERMISSION_ROLE_DELETE` |
| `/api/v1/admin/permissions/**` | GET | `ROLE_ADMIN` + `PERMISSION_READ` |
| `/api/v1/admin/permissions` | POST | `ROLE_ADMIN` + `PERMISSION_CREATE` |
| `/api/v1/admin/permissions/**` | PUT | `ROLE_ADMIN` + `PERMISSION_UPDATE` |
| `/api/v1/admin/permissions/**` | DELETE | `ROLE_ADMIN` + `PERMISSION_DELETE` |
| `/api/v1/admin/users/**` | GET | `ROLE_ADMIN` + `PERMISSION_USER_READ` |
| `/api/v1/admin/users/**` | PUT | `ROLE_ADMIN` + `PERMISSION_USER_MANAGE` |
| `/api/v1/admin/users/**` | POST/DELETE | `ROLE_ADMIN` + `PERMISSION_ROLE_ASSIGN` |
| Everything else | * | Authenticated |

---

## Configuration

### `application.yml` overview

```yaml
spring:
  application:
    name: api-gateway
  threads:
    virtual:
      enabled: true          # Java 25 virtual threads
  cloud:
    gateway:
      server:
        webmvc:
          routes: [...]      # see Route Configuration above
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

server:
  port: 8080

eureka:
  client:
    service-url:
      default-zone: ${EUREKA_URL:http://localhost:8761/eureka/}
  instance:
    prefer-ip-address: true

jwt:
  signing-key: ${JWT_SIGNING_KEY:...}
```

### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `JWT_SIGNING_KEY` | (dev fallback set in yml) | HMAC-SHA signing key shared with auth-service |
| `REDIS_HOST` | `localhost` | Redis server hostname |
| `REDIS_PORT` | `6379` | Redis server port |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | Eureka registry URL |

> The JWT signing key **must** match the key configured in `auth-service`. Any mismatch will cause all token validations to fail.

---

## Local Setup

### Prerequisites

- JDK 25
- Eureka server running on port 8761
- Redis running on port 6379

### Run via Gradle

```bash
# From project root
./gradlew :api-gateway:bootRun
```

### Run via Docker (infrastructure only)

Start PostgreSQL and Redis using the root `compose.yml`, then run the gateway separately:

```bash
docker compose up -d
./gradlew :api-gateway:bootRun
```

### Startup order

1. `eureka-server` — must be up first
2. `auth-service` — must be registered with Eureka before the gateway receives traffic
3. `api-gateway` — starts last; fails gracefully if Eureka is unavailable

---

## Dependencies

| Module | Relationship |
|---|---|
| `commons` | Compile-time — provides `ApiEndpoints`, `RoleCode`, `PermissionCode` |
| `eureka-server` | Runtime — service registry |
| `auth-service` | Runtime — downstream service (JWT is issued there) |
| Redis | Runtime — blacklist + user-block store |

---

## Health Check

```
GET http://localhost:8080/actuator/health
```
