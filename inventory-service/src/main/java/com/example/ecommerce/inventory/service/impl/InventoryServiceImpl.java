package com.example.ecommerce.inventory.service.impl;

import com.example.ecommerce.commons.exception.ResourceConflictException;
import com.example.ecommerce.inventory.client.ProductServiceClient;
import com.example.ecommerce.inventory.dto.request.InventoryCreateRequest;
import com.example.ecommerce.inventory.dto.request.InventoryQuantityRequest;
import com.example.ecommerce.inventory.dto.response.InventoryResponse;
import com.example.ecommerce.inventory.entity.Inventory;
import com.example.ecommerce.inventory.mapper.InventoryMapper;
import com.example.ecommerce.inventory.repository.InventoryRepository;
import com.example.ecommerce.inventory.service.InventoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductServiceClient productServiceClient;
    private final InventoryMapper inventoryMapper;

    @Override
    @Transactional
    public InventoryResponse create(InventoryCreateRequest request, Long userId) {
        // Validates product existence — FeignErrorDecoder propagates 404 as EntityNotFoundException
        productServiceClient.getProductById(request.productId());

        if (inventoryRepository.existsByProductId(request.productId())) {
            throw new ResourceConflictException(
                    "Inventory already exists for product: " + request.productId());
        }

        Inventory inventory = inventoryMapper.toEntity(request);
        inventory.setReservedQuantity(0);
        inventory.setCreatedBy(userId);
        inventory.setModifiedBy(userId);
        return inventoryMapper.toResponse(inventoryRepository.saveAndFlush(inventory));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getByProductId(Long productId) {
        return inventoryMapper.toResponse(findByProductId(productId));
    }

    @Override
    @Transactional
    public InventoryResponse increase(Long productId, InventoryQuantityRequest request, Long userId) {
        Inventory inventory = findByProductId(productId);
        inventory.setTotalQuantity(inventory.getTotalQuantity() + request.quantity());
        inventory.setModifiedBy(userId);
        return inventoryMapper.toResponse(inventoryRepository.saveAndFlush(inventory));
    }

    @Override
    @Transactional
    public InventoryResponse decrease(Long productId, InventoryQuantityRequest request, Long userId) {
        Inventory inventory = findByProductId(productId);
        int newTotal = inventory.getTotalQuantity() - request.quantity();
        if (newTotal < inventory.getReservedQuantity()) {
            throw new ResourceConflictException(
                    "Cannot decrease: total quantity would fall below reserved quantity ("
                    + inventory.getReservedQuantity() + ").");
        }
        inventory.setTotalQuantity(newTotal);
        inventory.setModifiedBy(userId);
        return inventoryMapper.toResponse(inventoryRepository.saveAndFlush(inventory));
    }

    @Override
    @Transactional
    public InventoryResponse reserve(Long productId, InventoryQuantityRequest request, Long userId) {
        Inventory inventory = findByProductId(productId);
        int available = inventory.getTotalQuantity() - inventory.getReservedQuantity();
        if (available < request.quantity()) {
            throw new ResourceConflictException(
                    "Insufficient available quantity. Available: " + available
                    + ", requested: " + request.quantity() + ".");
        }
        inventory.setReservedQuantity(inventory.getReservedQuantity() + request.quantity());
        inventory.setModifiedBy(userId);
        return inventoryMapper.toResponse(inventoryRepository.saveAndFlush(inventory));
    }

    @Override
    @Transactional
    public InventoryResponse release(Long productId, InventoryQuantityRequest request, Long userId) {
        Inventory inventory = findByProductId(productId);
        if (inventory.getReservedQuantity() < request.quantity()) {
            throw new ResourceConflictException(
                    "Cannot release: reserved quantity (" + inventory.getReservedQuantity()
                    + ") is less than requested release quantity (" + request.quantity() + ").");
        }
        inventory.setReservedQuantity(inventory.getReservedQuantity() - request.quantity());
        inventory.setModifiedBy(userId);
        return inventoryMapper.toResponse(inventoryRepository.saveAndFlush(inventory));
    }

    private Inventory findByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Inventory not found for product: " + productId));
    }
}
