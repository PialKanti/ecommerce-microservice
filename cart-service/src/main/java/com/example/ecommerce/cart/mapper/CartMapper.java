package com.example.ecommerce.cart.mapper;

import com.example.ecommerce.cart.dto.response.CartItemResponse;
import com.example.ecommerce.cart.dto.response.CartResponse;
import com.example.ecommerce.cart.entity.Cart;
import com.example.ecommerce.cart.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

    default CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(items)
                .totalQuantity(calculateTotalQuantity(cart))
                .subtotal(calculateSubtotal(cart))
                .createdAt(cart.getCreatedAt())
                .modifiedAt(cart.getModifiedAt())
                .createdBy(cart.getCreatedBy())
                .modifiedBy(cart.getModifiedBy())
                .build();
    }

    default CartResponse toEmptyResponse(Long userId) {
        return CartResponse.builder()
                .userId(userId)
                .items(List.of())
                .totalQuantity(0)
                .subtotal(0.0)
                .build();
    }

    private CartItemResponse toItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .sku(item.getProductSku())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getQuantity() * item.getUnitPrice())
                .build();
    }

    private Integer calculateTotalQuantity(Cart cart) {
        return cart.getItems()
                .stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    private Double calculateSubtotal(Cart cart) {
        return cart.getItems()
                .stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
    }
}
