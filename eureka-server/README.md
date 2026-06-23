# eureka-server

The Eureka Server is the service registry for the ecommerce microservices platform. All services register themselves here on startup, and the API Gateway queries it to resolve upstream service addresses by name — enabling load-balanced routing without hardcoded URLs.

---

## Architecture Role

```
eureka-server  (port 8761)
  │
  ├── api-gateway   → registers as "api-gateway", queries registry for routes
  └── auth-service  → registers as "auth-service"
      (all future services register here)
```

The API Gateway uses the logical name `lb://AUTH-SERVICE` in its route configuration. Eureka resolves this to a live instance address at request time.

---

## Key Features

- **Service registry** — acts as the discovery backbone; services register on startup and deregister on graceful shutdown.
- **Not self-registered** — `register-with-eureka: false` and `fetch-registry: false` prevent the server from registering with itself.
- **Self-preservation disabled** — `enable-self-preservation: false` is set for local development so stale registrations are evicted immediately rather than preserved during network partitions.
- **Zero sync wait** — `wait-time-in-ms-when-sync-empty: 0` allows the registry to begin serving immediately after startup without waiting for peer sync.
- **Actuator health endpoint** — `/actuator/health` for readiness checks.

---

## Technologies

| Technology | Version | Purpose |
|---|---|---|
| Spring Boot | 3.5.14 | Application framework |
| Spring Cloud Netflix Eureka Server | 2025.0.1 | Service registry implementation |
| Spring Boot Actuator | — | Health and info endpoints |
| Java Virtual Threads | JDK 25 | Inherited from root build config |

---

## Configuration

### `application.yml`

```yaml
spring:
  application:
    name: eureka-server

server:
  port: 8761

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false   # does not register itself
    fetch-registry: false         # does not fetch its own registry
    service-url:
      default-zone: http://localhost:8761/eureka/
  server:
    wait-time-in-ms-when-sync-empty: 0      # faster startup
    enable-self-preservation: false          # dev-friendly eviction

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

There are no environment variable overrides configured — this service runs with fixed settings suited for local and single-node environments. Update `eureka.instance.hostname` and the `default-zone` URL when deploying to a non-local environment.

---

## Local Setup

### Prerequisites

- JDK 25

### Run via Gradle

```bash
# From the project root
./gradlew :eureka-server:bootRun
```

The dashboard is available immediately after startup:

```
http://localhost:8761
```

### Startup order

Eureka Server must start **before** all other services. If auth-service or api-gateway start before Eureka is ready, they will retry registration automatically, but the API Gateway will not be able to route requests until auth-service is registered.

Recommended startup sequence:

1. `eureka-server`
2. `auth-service`
3. `api-gateway`

---

## Registered Services

Once all services are running, the Eureka dashboard at `http://localhost:8761` will show:

| Application | Port |
|---|---|
| `API-GATEWAY` | 8080 |
| `AUTH-SERVICE` | 8081 |

---

## Health Check

```
GET http://localhost:8761/actuator/health
```
