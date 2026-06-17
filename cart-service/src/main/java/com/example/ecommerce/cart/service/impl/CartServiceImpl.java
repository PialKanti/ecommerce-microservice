package com.example.ecommerce.cart.service.impl;

import com.example.ecommerce.cart.client.InventoryServiceClient;
import com.example.ecommerce.cart.client.ProductServiceClient;
import com.example.ecommerce.cart.client.dto.InventoryClientResponse;
import com.example.ecommerce.cart.client.dto.ProductClientResponse;
import com.example.ecommerce.cart.dto.request.CartItemAddRequest;
import com.example.ecommerce.cart.dto.response.CartResponse;
import com.example.ecommerce.cart.entity.Cart;
import com.example.ecommerce.cart.entity.CartItem;
import com.example.ecommerce.cart.mapper.CartMapper;
import com.example.ecommerce.cart.repository.CartRepository;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.commons.exception.ResourceConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;
    private final InventoryServiceClient inventoryServiceClient;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartResponse addItem(Long userId, CartItemAddRequest request) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> createCart(userId));

        Optional<CartItem> existingItem = findItemByProductId(cart, request.productId());
        int requestedCartQuantity = request.quantity() + existingItem.map(CartItem::getQuantity).orElse(0);

        ProductClientResponse product = validateAndFetchProduct(request.productId(), requestedCartQuantity);

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(requestedCartQuantity);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .productId(product.id())
                    .productSku(product.sku())
                    .productName(product.name())
                    .quantity(request.quantity())
                    .unitPrice(product.price())
                    .build();
            cart.getItems().add(item);
        }

        return cartMapper.toResponse(cartRepository.saveAndFlush(cart));
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .map(cartMapper::toResponse)
                .orElseGet(() -> cartMapper.toEmptyResponse(userId));
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.saveAndFlush(cart);
        });
    }

    /**
     * Fetches and validates product + inventory state via Feign before any cart
     * mutation happens. Kept isolated from the mutation logic above so a future
     * saga orchestrator can wrap or replace this validation step without touching
     * cart persistence.
     */
    private ProductClientResponse validateAndFetchProduct(Long productId, int requestedCartQuantity) {
        ProductClientResponse product = productServiceClient.getProductById(productId).getData();

        InventoryClientResponse inventory = inventoryServiceClient.getByProductId(productId).getData();

        if (requestedCartQuantity > inventory.availableQuantity()) {
            throw new ResourceConflictException("Insufficient available inventory for product: " + productId);
        }

        return product;
    }

    private Cart createCart(Long userId) {
        return Cart.builder()
                .userId(userId)
                .createdBy(userId)
                .modifiedBy(userId)
                .build();
    }

    private Optional<CartItem> findItemByProductId(Cart cart, Long productId) {
        return cart.getItems()
                .stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }
}
