package com.example.ecommerce.cart.client.dto;

public record ProductClientResponse(
        Long id,
        String sku,
        String name,
        Double price
) {
}
