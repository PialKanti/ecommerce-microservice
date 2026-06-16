package com.example.ecommerce.cart.service;

import com.example.ecommerce.cart.dto.request.CartItemAddRequest;
import com.example.ecommerce.cart.dto.response.CartResponse;

public interface CartService {

    CartResponse addItem(Long userId, CartItemAddRequest request);

    CartResponse getCart(Long userId);

    void clearCart(Long userId);
}
