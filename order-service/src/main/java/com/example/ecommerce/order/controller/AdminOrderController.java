package com.example.ecommerce.order.controller;

import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.dto.response.ApiResponse;
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
@RequestMapping(ApiEndpoints.Admin.BASE_ADMIN_ORDERS)
@RequiredArgsConstructor
@Tag(name = "Admin — Orders", description = "Administrative order support operations. Requires ADMIN or SUPPORT_AGENT role.")
@SecurityRequirement(name = "bearerAuth")
public class AdminOrderController {

    private final OrderService orderService;

    @Operation(summary = "Get any order by ID",
            description = "Retrieves any order regardless of owner. Requires PERMISSION_ORDER_READ.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @Parameter(description = "Order ID", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderForAdministration(id)));
    }

    @Operation(summary = "Cancel any confirmed order",
            description = "Cancels any confirmed order and releases reserved inventory. Requires PERMISSION_ORDER_CANCEL.")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Order ID", example = "1", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully.",
                orderService.cancelOrderForAdministration(userId, id)));
    }
}
