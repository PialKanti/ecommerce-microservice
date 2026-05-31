# auth-service

The auth-service is the identity and access management microservice for the ecommerce platform. It handles user registration and authentication, issues and rotates JWT access/refresh tokens, manages RBAC roles and permissions, and exposes user profile management endpoints. Security enforcement (filter chains, RBAC) lives in the API Gateway — this service is a plain Spring MVC application with no filter-chain security of its own.

---

## Architecture Role

```
api-gateway  (validates JWT, enforces RBAC)
     │
     ▼  (lb://AUTH-SERVICE via Eureka)
auth-service  (port 8081)
     │
     ├── PostgreSQL (auth_db)   — users, roles, permissions, refresh_tokens
     └── Redis                  — access token blacklist, user-block flags
```

Token issuance and validation both use the same HMAC-SHA signing key that is also configured in the API Gateway. The gateway validates the JWT; the auth-service issues it.

---

## Key Features

- **Registration & login** — BCrypt password hashing via `spring-security-crypto` (no filter chain).
- **JWT access tokens** — 15-minute lifetime, signed with HMAC-SHA; claims include user ID, username, roles, and permissions.
- **Rotating refresh tokens** — 7-day lifetime, stored as SHA-256 hashes in PostgreSQL. Each `/refresh` call revokes the current token and issues a new pair.
- **Logout with blacklisting** — on logout, the access token is SHA-256 hashed and written to Redis with a TTL matching its remaining validity. The API Gateway checks this key on every request.
- **User disable / block** — when an admin disables a user, a `auth:user:blocked:{userId}` key is written to Redis. The gateway rejects all requests from that user immediately, even with a valid token.
- **Full RBAC management** — CRUD for roles and permissions, with many-to-many assignment between them and between users and roles.
- **User profile management** — authenticated users can view and update their own profile and change their password.
- **Flyway migrations** — schema is version-controlled under `src/main/resources/db/migration`.
- **OpenAPI / Swagger UI** — full API documentation available at `/swagger-ui.html`.
- **Virtual threads** — enabled via `spring.threads.virtual.enabled: true` for high-concurrency I/O.

---

## Technologies

| Technology | Version | Purpose |
|---|---|---|
| Spring Boot | 3.5.14 | Application framework |
| Spring Data JPA + Hibernate | — | ORM, PostgreSQL dialect |
| Spring Security Crypto | — | BCrypt password encoding only |
| Flyway | — | Schema migrations |
| PostgreSQL | 17.5 | Primary data store |
| Spring Data Redis | — | Token blacklist, user-block flags |
| Netflix Eureka Client | 2025.0.1 | Service registration |
| JJWT | 0.12.6 | JWT creation and parsing |
| MapStruct | 1.6.3 | Entity ↔ DTO mapping |
| Springdoc OpenAPI | 2.8.16 | Swagger UI |
| Java Virtual Threads | JDK 25 | Concurrency |

---

## API Endpoints

### Authentication — `/api/v1/auth`

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Public | Register a new user account |
| POST | `/api/v1/auth/login` | Public | Authenticate and receive token pair |
| POST | `/api/v1/auth/refresh` | Public | Rotate refresh token, receive new pair |
| POST | `/api/v1/auth/logout` | Bearer token | Revoke refresh token + blacklist access token |

**Login response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "<jwt>",
    "refreshToken": "<opaque>",
    "tokenType": "Bearer"
  }
}
```

---

### User Profile — `/api/v1/users`

Requires a valid Bearer token. The API Gateway injects `X-User-Id` before the request reaches this service.

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/users/me` | Retrieve the authenticated user's profile |
| PUT | `/api/v1/users/me` | Update first name, last name, phone number |
| POST | `/api/v1/users/me/password` | Change password (current password verification required) |

---

### Admin — Role Management — `/api/v1/admin/roles`

Requires `ROLE_ADMIN` + appropriate `PERMISSION_ROLE_*` permission (enforced by the API Gateway).

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/admin/roles` | Paginated list of roles |
| GET | `/api/v1/admin/roles/{id}` | Get role by ID |
| POST | `/api/v1/admin/roles` | Create a new role |
| PUT | `/api/v1/admin/roles/{id}` | Update a role |
| DELETE | `/api/v1/admin/roles/{id}` | Delete a role |
| GET | `/api/v1/admin/roles/{roleId}/permissions` | List permissions assigned to a role |
| POST | `/api/v1/admin/roles/{roleId}/permissions/{permissionId}` | Assign a permission to a role |
| DELETE | `/api/v1/admin/roles/{roleId}/permissions/{permissionId}` | Remove a permission from a role |

---

### Admin — Permission Management — `/api/v1/admin/permissions`

Requires `ROLE_ADMIN` + appropriate `PERMISSION_*` permission.

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/admin/permissions` | Paginated list of permissions |
| GET | `/api/v1/admin/permissions/{id}` | Get permission by ID |
| POST | `/api/v1/admin/permissions` | Create a new permission |
| PUT | `/api/v1/admin/permissions/{id}` | Update a permission |
| DELETE | `/api/v1/admin/permissions/{id}` | Delete a permission |

---

### Admin — User Management — `/api/v1/admin/users`

Requires `ROLE_ADMIN` + appropriate `PERMISSION_USER_*` or `PERMISSION_ROLE_ASSIGN` permission.

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/admin/users` | Paginated user list with optional search + active filter |
| GET | `/api/v1/admin/users/{id}` | Get user by ID |
| PUT | `/api/v1/admin/users/{id}/status` | Activate or deactivate a user account |
| GET | `/api/v1/admin/users/{userId}/roles` | List roles assigned to a user |
| POST | `/api/v1/admin/users/{userId}/roles/{roleId}` | Assign a role to a user |
| DELETE | `/api/v1/admin/users/{userId}/roles/{roleId}` | Remove a role from a user |

---

## Configuration

### `application.yml` overview

```yaml
spring:
  application:
    name: auth-service
  threads:
    virtual:
      enabled: true
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/auth_db}
    username: ${DB_USERNAME:admin}
    password: ${DB_PASSWORD:admin@123}
  jpa:
    hibernate:
      ddl-auto: validate      # Flyway owns the schema; JPA only validates
  flyway:
    enabled: true
    locations: classpath:db/migration
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

server:
  port: 8081

jwt:
  signing-key: ${JWT_SIGNING_KEY:...}
  expiration: 15m
  refresh-expiration: 7d
```

### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/auth_db` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `admin` | PostgreSQL username |
| `DB_PASSWORD` | `admin@123` | PostgreSQL password |
| `JWT_SIGNING_KEY` | (dev fallback in yml) | HMAC-SHA signing key — must match the API Gateway |
| `REDIS_HOST` | `localhost` | Redis hostname |
| `REDIS_PORT` | `6379` | Redis port |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | Eureka registry URL |

---

## Local Setup

### Prerequisites

- JDK 25
- PostgreSQL 17 with a database named `auth_db`
- Redis 7.4
- Eureka server running on port 8761

### Start infrastructure with Docker

```bash
# From the project root — starts PostgreSQL and Redis
docker compose up -d
```

PostgreSQL init scripts in `docker/postgres/init/` create the `auth_db` database automatically.

### Run via Gradle

```bash
./gradlew :auth-service:bootRun
```

### Swagger UI

```
http://localhost:8081/swagger-ui.html
```

All endpoints, request/response schemas, and example payloads are documented there. Admin endpoints require a Bearer token obtained from `/api/v1/auth/login`.

---

## Redis Key Schema

| Key pattern | Written by | Purpose |
|---|---|---|
| `auth:blacklist:{sha256(token)}` | `AuthService.logout()` | Invalidate a logged-out access token |
| `auth:user:blocked:{userId}` | `AdminUserService.updateStatus()` | Block all requests from a disabled user |

---

## Dependencies

| Module / Service | Relationship |
|---|---|
| `commons` | Compile-time — `ApiEndpoints`, `ApiResponse`, `BaseEntity`, `RoleCode`, `PermissionCode` |
| `eureka-server` | Runtime — service registry |
| `api-gateway` | Runtime — proxies all inbound traffic; enforces RBAC before forwarding |
| PostgreSQL | Runtime — primary store |
| Redis | Runtime — token blacklist, user-block flags |

---

## Health Check

```
GET http://localhost:8081/actuator/health
```
