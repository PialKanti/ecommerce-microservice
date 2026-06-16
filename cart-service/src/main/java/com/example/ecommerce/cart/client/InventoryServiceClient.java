package com.example.ecommerce.cart.client;

import com.example.ecommerce.cart.client.dto.InventoryClientResponse;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "INVENTORY-SERVICE")
public interface InventoryServiceClient {

    @GetMapping("/api/v1/inventory/{productId}")
    ApiResponse<InventoryClientResponse> getByProductId(@PathVariable("productId") Long productId);
}
