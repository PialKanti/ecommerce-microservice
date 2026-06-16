package com.example.ecommerce.order.service;

import com.example.ecommerce.commons.exception.ResourceConflictException;
import com.example.ecommerce.order.client.InventoryServiceClient;
import com.example.ecommerce.order.client.dto.InventoryQuantityRequest;
import com.example.ecommerce.order.entity.Order;
import com.example.ecommerce.order.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCancellationService {

    private final InventoryServiceClient inventoryServiceClient;

    public void cancelConfirmedOrder(Order order, Long modifiedBy) {
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            log.warn("Order cancellation rejected for orderId={} because status={}", order.getId(), order.getStatus());
            throw new ResourceConflictException("Only confirmed orders can be cancelled.");
        }

        // SAGA compensation seam: release reserved inventory for each item
        order.getItems().forEach(item -> releaseInventory(item.getProductId(), item.getQuantity()));

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setModifiedBy(modifiedBy);
    }

    private void releaseInventory(Long productId, Integer quantity) {
        inventoryServiceClient.release(productId, new InventoryQuantityRequest(quantity));
    }
}
