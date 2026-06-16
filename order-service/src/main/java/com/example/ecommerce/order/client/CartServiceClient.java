package com.example.ecommerce.order.client;

import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.order.client.dto.CartClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "CART-SERVICE")
public interface CartServiceClient {

    @GetMapping(ApiEndpoints.Cart.BASE_CART)
    ApiResponse<CartClientResponse> getCart(@RequestHeader("X-User-Id") Long userId);

    @DeleteMapping(ApiEndpoints.Cart.BASE_CART)
    void clearCart(@RequestHeader("X-User-Id") Long userId);
}
