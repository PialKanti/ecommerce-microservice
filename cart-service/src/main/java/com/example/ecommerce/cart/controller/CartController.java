package com.example.ecommerce.cart.controller;

import com.example.ecommerce.cart.dto.request.CartItemAddRequest;
import com.example.ecommerce.cart.dto.response.CartResponse;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiEndpoints.Cart.BASE_CART)
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Authenticated user's own cart management. Requires a valid Bearer token.")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Add item to cart",
            description = "Adds an active product to the current user's cart after checking available inventory.")
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CartItemAddRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cartService.addItem(userId, request)));
    }

    @Operation(summary = "Get current cart",
            description = "Retrieves the current user's cart details including items, total quantity, and subtotal.")
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(userId)));
    }

    @Operation(summary = "Clear current cart",
            description = "Removes all items from the current user's cart. Idempotent — no-op if no cart exists.")
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
