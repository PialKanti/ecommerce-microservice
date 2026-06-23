package com.example.ecommerce.commons.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaidEvent {

    private UUID eventId;
    private Instant occurredAt;
    private Long orderId;
    private UUID orderNumber;
    private Long userId;
    private List<OrderItemPayload> items;
}
