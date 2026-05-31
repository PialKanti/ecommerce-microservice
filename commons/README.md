# commons

`commons` is a shared library module that provides the cross-cutting contracts and building blocks used by every service in the ecommerce microservices project. It is a plain Java library — it does not produce an executable JAR and has no Spring Boot plugin applied.

---

## Architecture Role

```
commons (plain library jar)
  │
  ├── api-gateway     (compile-time dependency)
  └── auth-service    (compile-time dependency)
  └── (all future services)
```

Any constant, type, or abstraction that must be consistent across service boundaries lives here. This prevents duplication and keeps inter-service contracts in one authoritative place.

---

## Contents

### DTOs

#### `ApiResponse<T>`

Uniform JSON envelope for all API responses.

```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;           // omitted when null (@JsonInclude NON_NULL)
}
```

Factory methods:
- `ApiResponse.success(data)` — wraps data with a generic success message
- `ApiResponse.success(message, data)` — wraps data with a custom message
- `ApiResponse.success(message)` — no data (e.g., logout)
- `ApiResponse.error(message)` — failure envelope

Example response shape:
```json
{
  "success": true,
  "message": "Login successful",
  "data": { ... }
}
```

#### `PaginatedResponse<T>`

Pagination wrapper backed by Spring Data's `Page<T>`.

```java
public class PaginatedResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
```

Built from any `Page<T>` via `PaginatedResponse.of(page)`.

---

### JPA Base Classes

#### `BaseEntity`

`@MappedSuperclass` — the root class for all persistent entities.

- Provides `id` (`Long`, auto-increment, immutable after insert)
- Implements `@PrePersist` / `@PreUpdate` lifecycle hooks that automatically set `createdAt` and `modifiedAt` on entities that implement `Auditable`

#### `Auditable`

Interface that `BaseEntity`'s lifecycle hooks check against. Entities that need audit timestamps and actor tracking implement this interface:

```java
public interface Auditable {
    LocalDateTime getCreatedAt();
    void setCreatedAt(LocalDateTime createdAt);

    LocalDateTime getModifiedAt();
    void setModifiedAt(LocalDateTime modifiedAt);

    Long getCreatedBy();
    void setCreatedBy(Long createdBy);

    Long getModifiedBy();
    void setModifiedBy(Long modifiedBy);
}
```

---

### Constants

#### `ApiEndpoints`

Centralized, non-instantiable class with path constants used by controllers in auth-service and by the `SecurityConfig` in api-gateway:

```java
ApiEndpoints.Auth.BASE_AUTH           // /api/v1/auth
ApiEndpoints.User.BASE_USERS          // /api/v1/users
ApiEndpoints.Admin.BASE_ADMIN         // /api/v1/admin
ApiEndpoints.Admin.BASE_ADMIN_ROLES        // /api/v1/admin/roles
ApiEndpoints.Admin.BASE_ADMIN_PERMISSIONS  // /api/v1/admin/permissions
ApiEndpoints.Admin.BASE_ADMIN_USERS        // /api/v1/admin/users
```

All controllers annotate `@RequestMapping` with these constants, and the gateway's `SecurityConfig` uses them for route matching. Changing a base path here propagates to both.

---

### Enums

#### `RoleCode`

Defines the valid role codes for the platform. Used in the gateway's RBAC authorization manager:

```java
public enum RoleCode {
    ADMIN,
    CUSTOMER,
    PRODUCT_MANAGER,
    INVENTORY_MANAGER,
    SUPPORT_AGENT
}
```

#### `PermissionCode`

Defines the complete set of fine-grained permission codes. Used as string authorities in JWTs and checked by the gateway's `roleAndPermission()` authorization manager:

| Category | Permissions |
|---|---|
| Product | `PERMISSION_PRODUCT_READ/CREATE/UPDATE/DELETE` |
| Category | `PERMISSION_CATEGORY_READ/CREATE/UPDATE/DELETE` |
| Inventory | `PERMISSION_INVENTORY_READ/MANAGE` |
| Order | `PERMISSION_ORDER_READ/UPDATE/CANCEL` |
| User | `PERMISSION_USER_READ/MANAGE` |
| Role | `PERMISSION_ROLE_READ/CREATE/UPDATE/DELETE/ASSIGN` |
| Permission | `PERMISSION_READ/CREATE/UPDATE/DELETE/ASSIGN` |

---

### Exceptions

#### `ResourceConflictException`

Thrown when a uniqueness constraint is violated (e.g., duplicate username or email during registration). Mapped to `409 Conflict` by the global exception handler in auth-service.

---

## Technologies

| Technology | Purpose |
|---|---|
| `jakarta.persistence-api` | JPA annotations for `BaseEntity` |
| `spring-data-commons` | `Page<T>` for `PaginatedResponse` |
| `jackson-annotations` | `@JsonInclude` on `ApiResponse` |
| Lombok | Boilerplate reduction on DTOs |

No Spring Boot plugin is applied — this module produces a plain library JAR, not a fat JAR.

---

## Local Setup

`commons` has no runnable entry point. It is built and consumed as a Gradle project dependency:

```gradle
// In any service build.gradle:
implementation project(':commons')
```

Build just the commons JAR:

```bash
./gradlew :commons:jar
```

Because `commons` is part of the multi-module Gradle build, it is compiled automatically whenever a dependent service is built or run.

---

## Adding New Shared Contracts

- **New endpoint prefix** → add a nested class to `ApiEndpoints`
- **New role** → add a value to `RoleCode`
- **New permission** → add a value to `PermissionCode`; then update the gateway `SecurityConfig` and the auth-service permission seed data accordingly
- **New shared DTO** → add to `com.example.ecommerce.commons.dto`
- **New shared exception** → add to `com.example.ecommerce.commons.exception`
