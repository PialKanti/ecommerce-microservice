package com.example.ecommerce.order.client;

import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.order.client.dto.InventoryClientResponse;
import com.example.ecommerce.order.client.dto.InventoryQuantityRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "INVENTORY-SERVICE")
public interface InventoryServiceClient {

    @PostMapping(ApiEndpoints.Admin.BASE_ADMIN_INVENTORY + "/{productId}/reserve")
    ApiResponse<InventoryClientResponse> reserve(
            @PathVariable Long productId,
            @RequestBody InventoryQuantityRequest request);

    @PostMapping(ApiEndpoints.Admin.BASE_ADMIN_INVENTORY + "/{productId}/release")
    ApiResponse<InventoryClientResponse> release(
            @PathVariable Long productId,
            @RequestBody InventoryQuantityRequest request);
}
