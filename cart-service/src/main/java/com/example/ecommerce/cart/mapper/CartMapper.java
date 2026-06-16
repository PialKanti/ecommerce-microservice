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

        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                items,
                calculateTotalQuantity(cart),
                calculateSubtotal(cart),
                cart.getCreatedAt(),
                cart.getModifiedAt(),
                cart.getCreatedBy(),
                cart.getModifiedBy()
        );
    }

    default CartResponse toEmptyResponse(Long userId) {
        return new CartResponse(null, userId, List.of(), 0, 0.0, null, null, null, null);
    }

    private CartItemResponse toItemResponse(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductSku(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getQuantity() * item.getUnitPrice()
        );
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
