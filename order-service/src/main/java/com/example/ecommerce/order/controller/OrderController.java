package com.example.ecommerce.order.controller;

import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.order.dto.response.CheckoutResponse;
import com.example.ecommerce.order.dto.response.OrderResponse;
import com.example.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiEndpoints.Order.BASE_ORDERS)
@RequiredArgsConstructor
@Tag(name = "Order", description = "Checkout, order lookup, and cancellation for authenticated users.")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Checkout cart",
            description = "Converts the current user's cart into a confirmed order and reserves inventory.")
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Order placed successfully.",
                orderService.placeOrder(userId)));
    }

    @Operation(summary = "Get order by ID",
            description = "Retrieves an order owned by the current user.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Order ID", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrder(userId, id)));
    }

    @Operation(summary = "Cancel order",
            description = "Cancels a confirmed order owned by the current user and releases reserved inventory.")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Order ID", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully.",
                orderService.cancelOrder(userId, id)));
    }
}
