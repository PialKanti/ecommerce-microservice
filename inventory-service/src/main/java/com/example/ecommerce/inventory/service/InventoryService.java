package com.example.ecommerce.inventory.service;

import com.example.ecommerce.inventory.dto.request.InventoryCreateRequest;
import com.example.ecommerce.inventory.dto.request.InventoryQuantityRequest;
import com.example.ecommerce.inventory.dto.response.InventoryResponse;

public interface InventoryService {

    InventoryResponse create(InventoryCreateRequest request, Long userId);

    InventoryResponse getByProductId(Long productId);

    InventoryResponse increase(Long productId, InventoryQuantityRequest request, Long userId);

    InventoryResponse decrease(Long productId, InventoryQuantityRequest request, Long userId);

    InventoryResponse reserve(Long productId, InventoryQuantityRequest request, Long userId);

    InventoryResponse release(Long productId, InventoryQuantityRequest request, Long userId);
}
