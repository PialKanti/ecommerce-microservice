package com.example.ecommerce.inventory.controller;

import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.inventory.dto.response.InventoryResponse;
import com.example.ecommerce.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiEndpoints.Inventory.BASE_INVENTORY)
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Public read-only access to inventory levels")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    @Operation(summary = "Get inventory by product ID")
    public ResponseEntity<ApiResponse<InventoryResponse>> getByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getByProductId(productId)));
    }
}
