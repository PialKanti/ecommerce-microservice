# Order Service — Checkout Workflow Diagrams

> **Pattern:** Choreography-based SAGA over RabbitMQ. Order-service does not call inventory-service
> synchronously at checkout. Instead it publishes domain events and reacts to replies, keeping each
> service independently deployable and failure-isolated.

---

## Table of Contents

1. [Event & Queue Reference](#event--queue-reference)
2. [Event Payload Details](#event-payload-details)
3. [Diagram 1 — High-Level Architecture](#diagram-1--high-level-architecture)
4. [Diagram 2 — Event Flow Map](#diagram-2--event-flow-map)
5. [Diagram 3 — Happy Path Sequence](#diagram-3--happy-path-sequence)
6. [Diagram 4 — Inventory Reservation Failure](#diagram-4--inventory-reservation-failure)
7. [Diagram 5 — Cart Clear Failure & Compensation](#diagram-5--cart-clear-failure--compensation)
8. [Diagram 6 — Order State Machine](#diagram-6--order-state-machine)

---

## Event & Queue Reference

All events travel through the **`ecommerce.topic`** Topic Exchange (RabbitMQ).
Every queue has a matching Dead-Letter Queue (`<name>.dlq`) backed by the **`ecommerce.dlx`** Direct Exchange.
Spring Retry delivers up to **3 attempts** (exponential back-off: 1 s → 10 s) before routing to the DLQ.

| # | Event Class | Routing Key | Queue Name | Publisher | Consumer |
|---|---|---|---|---|---|
| 1 | `OrderCreatedEvent` | `order.created` | `inventory.order.created` | order-service | inventory-service |
| 2 | `InventoryReservedEvent` | `inventory.reserved` | `order.inventory.reserved` | inventory-service | order-service |
| 3 | `InventoryReservationFailedEvent` | `inventory.reservation.failed` | `order.inventory.reservation.failed` | inventory-service | order-service |
| 4 | `OrderConfirmedEvent` | `order.confirmed` | `cart.order.confirmed` | order-service | cart-service |
| 5 | `CartClearFailedEvent` | `cart.clear.failed` | `order.cart.clear.failed` | cart-service | order-service |
| 6 | `OrderCancelledEvent` | `order.cancelled` | `inventory.order.cancelled` | order-service | inventory-service |

---

## Event Payload Details

| Event | Key Fields |
|---|---|
| `OrderCreatedEvent` | `eventId` (UUID), `occurredAt`, `orderId`, `orderNumber` (UUID), `userId`, `items` → `[{productId, quantity, unitPrice, productName}]` |
| `InventoryReservedEvent` | `eventId`, `occurredAt`, `orderId`, `orderNumber`, `userId` |
| `InventoryReservationFailedEvent` | `eventId`, `occurredAt`, `orderId`, `orderNumber`, `userId`, `reason` (String) |
| `OrderConfirmedEvent` | `eventId`, `occurredAt`, `orderId`, `orderNumber`, `userId` |
| `CartClearFailedEvent` | `eventId`, `occurredAt`, `orderId`, `orderNumber`, `userId`, `reason` (String) |
| `OrderCancelledEvent` | `eventId`, `occurredAt`, `orderId`, `orderNumber`, `userId`, `items` → `[{productId, quantity, ...}]`, `reason` (String) |

> **Idempotency:** Every consumer checks a `processed_events` table (`UUID eventId` PK) before handling
> an event and inserts after. A `DataIntegrityViolationException` on duplicate insert is silently swallowed
> to handle concurrent redeliveries. This prevents double-reservation, double-cancellation, etc.

---

## Diagram 1 — High-Level Architecture

Shows all participating services, their databases, the RabbitMQ topology, and the synchronous Feign call.

```mermaid
flowchart LR
    classDef svc fill:#1565C0,stroke:#0D47A1,color:#ffffff
    classDef db fill:#4A148C,stroke:#311B92,color:#ffffff
    classDef queue fill:#E65100,stroke:#BF360C,color:#ffffff
    classDef dlq fill:#7f3300,stroke:#4e1f00,color:#ffffff
    classDef exchange fill:#1B5E20,stroke:#1B5E20,color:#ffffff
    classDef gw fill:#00695C,stroke:#004D40,color:#ffffff
    classDef client fill:#455A64,stroke:#263238,color:#ffffff

    CLIENT(["Client\nBrowser / Mobile App"]):::client
    GW["API Gateway\n:8080\nJWT · RBAC · User-Context Headers"]:::gw

    subgraph ORDER["Order Service  :8085"]
        OS["OrderController\nOrderEventListener\nOrderCancellationService"]:::svc
        OS_DB[("order_db\nPostgreSQL")]:::db
        OS --- OS_DB
    end

    subgraph MQ["RabbitMQ  :5672   —   ecommerce.topic  (Topic Exchange)"]
        direction TB
        Q1["inventory.order.created\nrk: order.created"]:::queue
        Q2["order.inventory.reserved\nrk: inventory.reserved"]:::queue
        Q3["order.inventory.reservation.failed\nrk: inventory.reservation.failed"]:::queue
        Q4["cart.order.confirmed\nrk: order.confirmed"]:::queue
        Q5["order.cart.clear.failed\nrk: cart.clear.failed"]:::queue
        Q6["inventory.order.cancelled\nrk: order.cancelled"]:::queue
        DLQ["*.dlq  (Dead-Letter Queues)\nbacked by ecommerce.dlx exchange\nafter 3 failed retries"]:::dlq
    end

    subgraph INV["Inventory Service  :8083"]
        IS["InventoryEventListener\nStock Reservation / Release"]:::svc
        IS_DB[("inventory_db\nPostgreSQL")]:::db
        IS --- IS_DB
    end

    subgraph CART["Cart Service  :8084"]
        CS["CartController\nCartEventListener\nCart Clear"]:::svc
        CS_DB[("cart_db\nPostgreSQL")]:::db
        CS --- CS_DB
    end

    CLIENT -->|"Bearer JWT"| GW
    GW -->|"X-User-Id header"| OS

    OS -->|"1  OrderCreatedEvent"| Q1
    OS -->|"4  OrderConfirmedEvent"| Q4
    OS -->|"6  OrderCancelledEvent"| Q6
    Q2 -->|"2a  InventoryReservedEvent"| OS
    Q3 -->|"2b  InventoryReservationFailedEvent"| OS
    Q5 -->|"5  CartClearFailedEvent"| OS

    Q1 -->|"consume"| IS
    Q6 -->|"consume"| IS
    IS -->|"2a  InventoryReservedEvent"| Q2
    IS -->|"2b  InventoryReservationFailedEvent"| Q3

    Q4 -->|"consume"| CS
    CS -->|"5  CartClearFailedEvent"| Q5

    Q1 & Q2 & Q3 & Q4 & Q5 & Q6 -.->|"failed after retries"| DLQ

    OS <-->|"Feign sync\nGET /api/v1/cart\n(checkout only)"| CS
```

---

## Diagram 2 — Event Flow Map

Simplified view: which service publishes which event, which queue carries it, and who consumes it.
Numbers indicate the typical order of events in the happy path.

```mermaid
flowchart TB
    classDef os fill:#1565C0,stroke:#0D47A1,color:#ffffff,rx:12
    classDef is fill:#2E7D32,stroke:#1B5E20,color:#ffffff,rx:12
    classDef cs fill:#6A1B9A,stroke:#4A148C,color:#ffffff,rx:12
    classDef queue fill:#E65100,stroke:#BF360C,color:#ffffff

    OS(["Order Service"]):::os
    IS(["Inventory Service"]):::is
    CS(["Cart Service"]):::cs

    OS -->|"1  OrderCreatedEvent\n  Q: inventory.order.created\n  rk: order.created"| IS

    IS -->|"2a  InventoryReservedEvent\n  Q: order.inventory.reserved\n  rk: inventory.reserved"| OS

    IS -->|"2b  InventoryReservationFailedEvent\n  Q: order.inventory.reservation.failed\n  rk: inventory.reservation.failed"| OS

    OS -->|"3  OrderConfirmedEvent  (only after 2a)\n  Q: cart.order.confirmed\n  rk: order.confirmed"| CS

    CS -->|"4  CartClearFailedEvent  (only if clear fails)\n  Q: order.cart.clear.failed\n  rk: cart.clear.failed"| OS

    OS -->|"5  OrderCancelledEvent  (compensation)\n  Q: inventory.order.cancelled\n  rk: order.cancelled"| IS
```

---

## Diagram 3 — Happy Path Sequence

**Scenario:** Customer checks out. Cart is non-empty. All items are in stock. Cart clears successfully.
**Result:** `Order = CONFIRMED`, inventory reserved, cart cleared.

```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant GW as API Gateway :8080
    participant OS as Order Service :8085
    participant CS as Cart Service :8084
    participant MQ as RabbitMQ
    participant IS as Inventory Service :8083

    C->>GW: POST /api/v1/orders/checkout<br/>[Authorization: Bearer JWT]
    Note over GW: Validate JWT signature + expiry<br/>Check Redis blacklist<br/>Check Redis user-block flag<br/>Inject X-User-Id header
    GW->>OS: Forward request [X-User-Id: {userId}]

    OS->>CS: Feign: GET /api/v1/cart  [X-User-Id: {userId}]
    CS-->>OS: CartResponse {items, totalQuantity, subtotal}

    OS->>OS: Build Order (status=PENDING)<br/>Snapshot CartItems → OrderItems<br/>Generate random orderNumber (UUID)
    OS->>OS: saveAndFlush(order)

    OS->>MQ: publish OrderCreatedEvent<br/>exchange: ecommerce.topic  rk: order.created<br/>→ Q: inventory.order.created<br/>payload: {orderId, orderNumber, userId, items[{productId, qty, price}]}
    OS-->>C: HTTP 200  Order {id, status=PENDING, orderNumber}

    Note over C: Client polls GET /api/v1/orders/{id}<br/>until status changes from PENDING

    MQ->>IS: deliver OrderCreatedEvent<br/>from Q: inventory.order.created
    Note over IS: Check processed_events table<br/>Skip if eventId already seen
    IS->>IS: reserveItem(productId, qty) per item<br/>Increment inventory.reservedQuantity<br/>(optimistic lock via @Version)
    IS->>IS: INSERT processed_event(eventId)
    IS->>MQ: publish InventoryReservedEvent<br/>exchange: ecommerce.topic  rk: inventory.reserved<br/>→ Q: order.inventory.reserved<br/>payload: {orderId, orderNumber, userId}

    MQ->>OS: deliver InventoryReservedEvent<br/>from Q: order.inventory.reserved
    Note over OS: Check processed_events table<br/>Verify order.status == PENDING
    OS->>OS: order.status = CONFIRMED
    OS->>OS: saveAndFlush(order)
    OS->>MQ: publish OrderConfirmedEvent<br/>exchange: ecommerce.topic  rk: order.confirmed<br/>→ Q: cart.order.confirmed<br/>payload: {orderId, orderNumber, userId}
    OS->>OS: INSERT processed_event(eventId)

    MQ->>CS: deliver OrderConfirmedEvent<br/>from Q: cart.order.confirmed
    Note over CS: Check processed_events table
    CS->>CS: clearCart(userId) — deletes cart + items
    CS->>CS: INSERT processed_event(eventId)

    Note over C,IS: ✅ FINAL STATE<br/>Order = CONFIRMED  |  Inventory = reserved  |  Cart = cleared
```

---

## Diagram 4 — Inventory Reservation Failure

**Scenario:** One or more items do not have enough available stock.
**Result:** `Order = CANCELLED`, inventory unchanged, cart intact.

```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant GW as API Gateway :8080
    participant OS as Order Service :8085
    participant CS as Cart Service :8084
    participant MQ as RabbitMQ
    participant IS as Inventory Service :8083

    C->>GW: POST /api/v1/orders/checkout<br/>[Authorization: Bearer JWT]
    GW->>OS: Forward request [X-User-Id: {userId}]
    OS->>CS: Feign: GET /api/v1/cart  [X-User-Id: {userId}]
    CS-->>OS: CartResponse {items, ...}
    OS->>OS: Build Order (status=PENDING)<br/>saveAndFlush(order)
    OS->>MQ: publish OrderCreatedEvent<br/>rk: order.created → Q: inventory.order.created
    OS-->>C: HTTP 200  Order {status=PENDING}

    MQ->>IS: deliver OrderCreatedEvent<br/>from Q: inventory.order.created
    Note over IS: Check processed_events table
    IS->>IS: reserveItem() — FAILS<br/>available < requested quantity<br/>throws IllegalStateException
    IS->>IS: INSERT processed_event(eventId)
    IS->>MQ: publish InventoryReservationFailedEvent<br/>rk: inventory.reservation.failed<br/>→ Q: order.inventory.reservation.failed<br/>payload: {orderId, userId, reason: "Insufficient stock for product X:<br/>available=2, requested=5"}

    MQ->>OS: deliver InventoryReservationFailedEvent<br/>from Q: order.inventory.reservation.failed
    Note over OS: Check processed_events table<br/>Verify order.status == PENDING
    OS->>OS: OrderCancellationService.cancelAndPublish()<br/>order.status = CANCELLED<br/>order.cancelledAt = now()
    OS->>OS: saveAndFlush(order)
    OS->>MQ: publish OrderCancelledEvent<br/>rk: order.cancelled → Q: inventory.order.cancelled<br/>payload: {orderId, items, reason}
    OS->>OS: INSERT processed_event(eventId)

    MQ->>IS: deliver OrderCancelledEvent<br/>from Q: inventory.order.cancelled
    Note over IS: Check processed_events table
    IS->>IS: releaseItem() — NO-OP<br/>reservedQuantity was never incremented<br/>Math.min(requested, reservedQty) = 0
    IS->>IS: INSERT processed_event(eventId)

    Note over C,IS: ❌ FINAL STATE<br/>Order = CANCELLED  |  Inventory = unchanged  |  Cart = intact

    C->>OS: GET /api/v1/orders/{id}
    OS-->>C: Order {status=CANCELLED, cancelledAt}
```

---

## Diagram 5 — Cart Clear Failure & Compensation

**Scenario:** Inventory reservation succeeds (order is CONFIRMED), but the cart-clear step fails
(e.g. DB error). The SAGA compensates by cancelling the now-confirmed order and releasing the
reserved stock.
**Result:** `Order = CANCELLED`, inventory stock released, cart may still contain items.

```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant OS as Order Service :8085
    participant CS as Cart Service :8084
    participant MQ as RabbitMQ
    participant IS as Inventory Service :8083

    Note over C,IS: Steps 1–7 identical to Happy Path<br/>(Order created, inventory reserved, order status = CONFIRMED)

    OS->>MQ: publish OrderConfirmedEvent<br/>rk: order.confirmed → Q: cart.order.confirmed<br/>payload: {orderId, orderNumber, userId}

    MQ->>CS: deliver OrderConfirmedEvent<br/>from Q: cart.order.confirmed
    Note over CS: Check processed_events table
    CS->>CS: clearCart(userId) — FAILS<br/>e.g. DB timeout, constraint violation
    CS->>MQ: publish CartClearFailedEvent<br/>rk: cart.clear.failed → Q: order.cart.clear.failed<br/>payload: {orderId, userId, reason: "..."}
    CS->>CS: INSERT processed_event(eventId)

    MQ->>OS: deliver CartClearFailedEvent<br/>from Q: order.cart.clear.failed
    Note over OS: Check processed_events table<br/>Verify order.status == CONFIRMED
    OS->>OS: OrderCancellationService.cancelAndPublish()<br/>order.status = CANCELLED<br/>order.cancelledAt = now()
    OS->>OS: saveAndFlush(order)
    OS->>MQ: publish OrderCancelledEvent<br/>rk: order.cancelled → Q: inventory.order.cancelled<br/>payload: {orderId, items, reason: "Cart clear failed: ..."}
    OS->>OS: INSERT processed_event(eventId)

    MQ->>IS: deliver OrderCancelledEvent<br/>from Q: inventory.order.cancelled
    Note over IS: Check processed_events table
    IS->>IS: releaseItem() per item<br/>Decrement inventory.reservedQuantity<br/>Math.min(requested, current reservedQty) — safe cap
    IS->>IS: INSERT processed_event(eventId)

    Note over C,IS: ❌ FINAL STATE (Compensated)<br/>Order = CANCELLED  |  Inventory = released  |  Cart = may still contain items
```

---

## Diagram 6 — Order State Machine

All possible `OrderStatus` transitions and the trigger for each.

```mermaid
stateDiagram-v2
    direction LR

    [*] --> PENDING : POST /checkout\n(saveAndFlush)

    PENDING --> CONFIRMED : InventoryReservedEvent received\n(OrderEventListener)
    PENDING --> CANCELLED : InventoryReservationFailedEvent received\n(OrderEventListener)
    PENDING --> CANCELLED : User cancel: POST /orders/{id}/cancel\nAdmin cancel: POST /admin/orders/{id}/cancel

    CONFIRMED --> CANCELLED : CartClearFailedEvent received\n(OrderEventListener)
    CONFIRMED --> CANCELLED : User cancel: POST /orders/{id}/cancel\nAdmin cancel: POST /admin/orders/{id}/cancel
    CONFIRMED --> PAID : [Reserved for future payment-service]

    CANCELLED --> [*]
    PAID --> [*]

    note right of PENDING
        OrderCreatedEvent published.
        Inventory reservation in flight.
        Client polls GET /orders/{id}.
    end note

    note right of CONFIRMED
        InventoryReservedEvent received.
        OrderConfirmedEvent published.
        Cart-clear in flight.
        Stock is reserved in inventory_db.
    end note

    note right of CANCELLED
        OrderCancelledEvent published.
        Inventory stock released (if it was reserved).
        cancelledAt timestamp set.
    end note
```

---

## Compensation Summary

| Trigger | Order transitions | Compensation action |
|---|---|---|
| Insufficient stock | `PENDING → CANCELLED` | `OrderCancelledEvent` → inventory releases (no-op, nothing was reserved) |
| Cart clear fails | `CONFIRMED → CANCELLED` | `OrderCancelledEvent` → inventory releases reserved stock |
| User/Admin cancel | `PENDING → CANCELLED` | `OrderCancelledEvent` → inventory releases (safe-capped) |
| User/Admin cancel | `CONFIRMED → CANCELLED` | `OrderCancelledEvent` → inventory releases reserved stock |

> **Idempotency guarantee:** If any event is redelivered (network retry, consumer restart), the
> `processed_events` table prevents double-processing. A `DataIntegrityViolationException` on
> concurrent insert is silently swallowed — the first writer wins.
