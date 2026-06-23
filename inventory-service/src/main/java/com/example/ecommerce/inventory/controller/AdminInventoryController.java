package com.example.ecommerce.inventory.controller;

import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.inventory.dto.request.InventoryCreateRequest;
import com.example.ecommerce.inventory.dto.request.InventoryQuantityRequest;
import com.example.ecommerce.inventory.dto.response.InventoryResponse;
import com.example.ecommerce.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiEndpoints.Admin.BASE_ADMIN_INVENTORY)
@RequiredArgsConstructor
@Tag(name = "Admin — Inventory", description = "Write operations. Requires INVENTORY_MANAGER or ADMIN role.")
@SecurityRequirement(name = "bearerAuth")
public class AdminInventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @Operation(summary = "Create inventory for a product")
    public ResponseEntity<ApiResponse<InventoryResponse>> create(
            @Valid @RequestBody InventoryCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Inventory created.",
                inventoryService.create(request, userId)));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get inventory by product ID (admin)")
    public ResponseEntity<ApiResponse<InventoryResponse>> getByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getByProductId(productId)));
    }

    @PostMapping("/{productId}/increase")
    @Operation(summary = "Increase total stock quantity")
    public ResponseEntity<ApiResponse<InventoryResponse>> increase(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryQuantityRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Stock increased.",
                inventoryService.increase(productId, request, userId)));
    }

    @PostMapping("/{productId}/decrease")
    @Operation(summary = "Decrease total stock quantity")
    public ResponseEntity<ApiResponse<InventoryResponse>> decrease(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryQuantityRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Stock decreased.",
                inventoryService.decrease(productId, request, userId)));
    }

    @PostMapping("/{productId}/reserve")
    @Operation(summary = "Reserve available stock (used during order creation)")
    public ResponseEntity<ApiResponse<InventoryResponse>> reserve(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryQuantityRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Stock reserved.",
                inventoryService.reserve(productId, request, userId)));
    }

    @PostMapping("/{productId}/release")
    @Operation(summary = "Release reserved stock (used during order cancellation)")
    public ResponseEntity<ApiResponse<InventoryResponse>> release(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryQuantityRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Stock released.",
                inventoryService.release(productId, request, userId)));
    }
}
